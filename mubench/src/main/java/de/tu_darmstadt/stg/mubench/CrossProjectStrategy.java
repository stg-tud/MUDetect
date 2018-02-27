package de.tu_darmstadt.stg.mubench;

import com.google.common.collect.Multiset;
import de.tu_darmstadt.stg.mubench.cli.DetectionStrategy;
import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mudetect.*;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import edu.iastate.cs.mudetect.mining.AUGMiner;
import edu.iastate.cs.mudetect.mining.DefaultAUGMiner;
import edu.iastate.cs.mudetect.mining.MinPatternActionsModel;
import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.ranking.*;
import de.tu_darmstadt.stg.mustudies.UsageUtils;
import de.tu_darmstadt.stg.yaml.YamlObject;
import edu.iastate.cs.egroum.aug.TypeUsageExamplePredicate;
import edu.iastate.cs.egroum.aug.AUGBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class CrossProjectStrategy implements DetectionStrategy {
    @Override
    public DetectorOutput detectViolations(DetectorArgs args, DetectorOutput.Builder output) throws Exception {
        TargetProject targetProject = TargetProject.find(getIndexFilePath(), args.getTargetSrcPaths());
        Collection<APIUsageExample> targets = loadDetectionTargets(args, targetProject);
        output.withRunInfo("numberOfTargets", targets.size());

        Set<APIUsagePattern> patterns = new HashSet<>();
        Set<String> minedForAPIs = new HashSet<>();
        for (Misuse misuse : targetProject.getMisuses()) {
            API api = misuse.getMisusedAPI();

            String logPrefix;
            TypeUsageExamplePredicate examplePredicate;
            if (minedForAPIs.contains(api.getName()))
                continue;

            System.out.println(String.format("[MuDetectXProject] Target API = %s", api));
            examplePredicate = TypeUsageExamplePredicate.usageExamplesOf(api.getName());
            logPrefix = api.getSimpleName();
            minedForAPIs.add(api.getName());

            Collection<APIUsageExample> trainingExamples = loadTrainingExamples(api, logPrefix, examplePredicate, args, output);
            output.withRunInfo(logPrefix + "-numberOfTrainingExamples", trainingExamples.size());
            output.withRunInfo(logPrefix + "-numberOfUsagesInTrainingExamples", getTypeUsageCounts(trainingExamples));

            Model model = createMiner().mine(trainingExamples);
            output.withRunInfo(logPrefix + "-numberOfPatterns", model.getPatterns().size());
            output.withRunInfo(logPrefix + "-maxPatternSupport", model.getMaxPatternSupport());

            patterns.addAll(model.getPatterns());
        }

        Model model = () -> patterns;
        List<Violation> violations = createDetector(model).findViolations(targets);
        output.withRunInfo("numberOfViolations", violations.size());
        output.withRunInfo("numberOfExploredAlternatives", AlternativeMappingsOverlapsFinder.numberOfExploredAlternatives);

        return output.withFindings(violations, ViolationUtils::toFinding);
    }

    private Collection<APIUsageExample> loadTrainingExamples(API targetType, String logPrefix, TypeUsageExamplePredicate examplePredicate, DetectorArgs args, DetectorOutput.Builder output) {
        List<ExampleProject> exampleProjects = getExampleProjects(targetType);
        System.out.println(String.format("[MuDetectXProject] Example Projects = %d", exampleProjects.size()));
        output.withRunInfo(logPrefix + "-exampleProjects", exampleProjects.size());

        AUGBuilder builder = new AUGBuilder(new DefaultAUGConfiguration() {{
            usageExamplePredicate = examplePredicate;
        }});
        List<APIUsageExample> targetTypeExamples = new ArrayList<>();
        for (ExampleProject exampleProject : exampleProjects) {
            String projectName = exampleProject.getProjectPath();
            List<APIUsageExample> projectExamples = new ArrayList<>();
            for (String srcDir : exampleProject.getSrcDirs()) {
                Path projectSrcPath = Paths.get(exampleProject.getProjectPath(), srcDir);
                System.out.println(String.format("[MuDetectXProject] Scanning path %s", projectSrcPath));
                PrintStream originalSysOut = System.out;
                try {
                    System.setOut(new PrintStream(new OutputStream() {
                        @Override
                        public void write(int arg0) {}
                    }));
                    projectExamples.addAll(builder.build(projectSrcPath.toString(), args.getDependencyClassPath()));
                } catch (Exception e) {
                    System.err.print("[MuDetectXProject] Parsing failed: ");
                    e.printStackTrace(System.err);
                } finally {
                    System.setOut(originalSysOut);
                }
            }
            System.out.println(String.format("[MuDetectXProject] Examples from Project = %d", projectExamples.size()));
            int maxNumberOfExamplesPerProject = 1000 / exampleProjects.size();
            if (projectExamples.size() > maxNumberOfExamplesPerProject) {
                projectExamples = pickNRandomElements(projectExamples, maxNumberOfExamplesPerProject, new Random(projectName.hashCode()));
                System.out.println(String.format("[MuDetectXProject] Too many examples, sampling %d.", maxNumberOfExamplesPerProject));
            }

            targetTypeExamples.addAll(projectExamples);
        }
        System.out.println(String.format("[MuDetectXProject] Examples = %d", targetTypeExamples.size()));

        return targetTypeExamples;
    }

    private List<ExampleProject> getExampleProjects(API targetType) {
        Path dataFile = Paths.get(getExamplesBasePath().toString(), targetType + ".yml");
        try (InputStream is = new FileInputStream(dataFile.toFile())) {
            return StreamSupport.stream(new Yaml().loadAll(is).spliterator(), false)
                    .map(ExampleProject::create).collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalArgumentException("failed to load example data for " + targetType, e);
        }
    }

    static class ExampleProject {
        private final String projectPath;
        private final List<String> srcDirs;

        ExampleProject(String projectPath, List<String> srcDirs) {
            this.projectPath = projectPath;
            this.srcDirs = srcDirs;
        }

        @SuppressWarnings("unchecked")
        static ExampleProject create(Object yamlSpec) {
            Map<String, Object> data = (Map<String, Object>) yamlSpec;
            String projectPath = (String) data.get("path");
            List<String> srcDirs = (List<String>) data.get("source_paths");
            return new ExampleProject(projectPath, srcDirs);
        }

        String getProjectPath() {
            return projectPath;
        }

        List<String> getSrcDirs() {
            return srcDirs;
        }
    }

    private Path getIndexFilePath() throws FileNotFoundException {
        Path path = Paths.get(getExamplesBasePath().toString(), "index.csv");
        if (!Files.exists(path)) {
            throw new FileNotFoundException("No index file '" + path + "'.");
        }
        return path;
    }

    private Path getExamplesBasePath() {
        return Paths.get(getMuBenchBasePath().toString(), "checkouts-xp");
    }

    private Path getMuBenchBasePath() {
        return Paths.get(".");
    }

    private static <E> List<E> pickNRandomElements(List<E> list, int n, Random r) {
        int length = list.size();
        if (length < n) return list;

        for (int i = length - 1; i >= length - n; --i) {
            Collections.swap(list, i , r.nextInt(i + 1));
        }
        return list.subList(length - n, length);
    }

    private YamlObject getTypeUsageCounts(Collection<APIUsageExample> targets) {
        YamlObject object = new YamlObject();
        for (Multiset.Entry<String> entry : UsageUtils.countNumberOfUsagesPerType(targets).entrySet()) {
            object.put(entry.getElement(), entry.getCount());
        }
        return object;
    }

    private AUGMiner createMiner() {
        return new DefaultAUGMiner(new DefaultMiningConfiguration() {{
            occurenceLevel = Level.CROSS_PROJECT;
            minPatternSupport = 5;
        }});
    }

    private Collection<APIUsageExample> loadDetectionTargets(DetectorArgs args, TargetProject targetProject) throws IOException {
        AUGBuilder builder = new AUGBuilder(new DefaultAUGConfiguration() {{
            usageExamplePredicate = MisuseInstancePredicate.examplesOf(targetProject.getMisuses());
        }});
        return builder.build(args.getTargetSrcPaths(), args.getDependencyClassPath());
    }

    private MuDetect createDetector(Model model) {
        return new MuDetect(
                new MinPatternActionsModel(model, 2),
                new AlternativeMappingsOverlapsFinder(new DefaultOverlapFinderConfig(new DefaultMiningConfiguration())),
                new FirstDecisionViolationPredicate(
                        new MissingDefPrefixNoViolationPredicate(),
                        new OnlyDefPrefixNoViolationPredicate(),
                        new MissingCatchNoViolationPredicate(),
                        new MissingAssignmentNoViolationPredicate(),
                        new MissingElementViolationPredicate()),
                new DefaultFilterAndRankingStrategy(
                        new WeightRankingStrategy(
                                    new ProductWeightFunction(
                                            new OverlapWithoutEdgesToMissingNodesWeightFunction(new ConstantNodeWeightFunction()),
                                            new PatternSupportWeightFunction(),
                                            new ViolationSupportWeightFunction()
                                    ))));
    }
}
