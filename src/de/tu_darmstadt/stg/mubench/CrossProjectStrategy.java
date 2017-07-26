package de.tu_darmstadt.stg.mubench;

import com.google.common.collect.Multiset;
import de.tu_darmstadt.stg.mubench.cli.DetectionStrategy;
import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mudetect.FirstDecisionViolationPredicate;
import de.tu_darmstadt.stg.mudetect.MissingElementViolationPredicate;
import de.tu_darmstadt.stg.mudetect.MuDetect;
import de.tu_darmstadt.stg.mudetect.OptionalDefPrefixViolationPredicate;
import de.tu_darmstadt.stg.mudetect.mining.*;
import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.ranking.*;
import de.tu_darmstadt.stg.mustudies.UsageUtils;
import de.tu_darmstadt.stg.yaml.YamlObject;
import egroum.AUGBuilder;
import egroum.EGroumBuilder;
import egroum.EGroumGraph;
import mining.TypeUsageExamplePredicate;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class CrossProjectStrategy implements DetectionStrategy {
    private final Mode mode;

    public static enum Mode {
        OFFLINE, ONLINE
    }

    public CrossProjectStrategy(Mode mode) {
        this.mode = mode;
    }

    @Override
    public DetectorOutput detectViolations(DetectorArgs args) throws Exception {
        DetectorOutput.Builder output = createOutput();

        TargetProject targetProject = TargetProject.find(getIndexFilePath(), args.getTargetPath());
        Collection<AUG> targets = loadDetectionTargets(args, targetProject);
        output.withRunInfo("numberOfTargets", targets.size());

        Set<Pattern> patterns = new HashSet<>();
        Set<String> minedForAPIs = new HashSet<>();
        for (Misuse misuse : targetProject.getMisuses()) {
            API api = misuse.getMisusedAPI();

            String logPrefix;
            TypeUsageExamplePredicate examplePredicate;
            switch (mode) {
                case OFFLINE:
                    if (minedForAPIs.contains(api.getName()))
                        continue;

                    System.out.println(String.format("[MuDetectXProject] Target API = %s", api));
                    examplePredicate = TypeUsageExamplePredicate.usageExamplesOf(api.getName());
                    logPrefix = api.getSimpleName();
                    minedForAPIs.add(api.getName());
                    break;
                case ONLINE:

                    System.out.println(String.format("[MuDetectXProject] Target API = %s, Misuse = %s", api, misuse.getId()));
                    AUG misuseInstance = findMisuseInstance(misuse, targets);
                    examplePredicate = SimilarUsageExamplePredicate.examplesSimilarTo(misuseInstance, api);
                    logPrefix = "M-" + misuse.getId() + "-" + api.getSimpleName();
                    break;
                default:
                    throw new IllegalStateException("no such mode: " + mode);
            }

            Collection<EGroumGraph> trainingExamples = loadTrainingExamples(api, logPrefix, examplePredicate, args, output);
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

    private AUG findMisuseInstance(Misuse misuse, Collection<AUG> targets) {
        for (AUG target : targets) {
            if (target.getLocation().getMethodName().equals(misuse.getMethodSignature())) {
                return target;
            }
        }
        throw new IllegalStateException("no target for misuse.");
    }

    private Collection<EGroumGraph> loadTrainingExamples(API targetType, String logPrefix, TypeUsageExamplePredicate examplePredicate, DetectorArgs args, DetectorOutput.Builder output) throws FileNotFoundException {
        List<ExampleProject> exampleProjects = getExampleProjects(targetType);
        System.out.println(String.format("[MuDetectXProject] Example Projects = %d", exampleProjects.size()));
        output.withRunInfo(logPrefix + "-exampleProjects", exampleProjects.size());

        EGroumBuilder builder = new EGroumBuilder(new DefaultAUGConfiguration() {{
            usageExamplePredicate = examplePredicate;
        }});
        List<EGroumGraph> targetTypeExamples = new ArrayList<>();
        for (ExampleProject exampleProject : exampleProjects) {
            String projectName = exampleProject.getProjectPath();
            List<EGroumGraph> projectExamples = new ArrayList<>();
            for (String srcDir : exampleProject.getSrcDirs()) {
                Path projectSrcPath = Paths.get(exampleProject.getProjectPath(), srcDir);
                System.out.println(String.format("[MuDetectXProject] Scanning path %s", projectSrcPath));
                PrintStream originalSysOut = System.out;
                try {
                    System.setOut(new PrintStream(new OutputStream() {
                        @Override
                        public void write(int arg0) throws IOException {}
                    }));
                    projectExamples.addAll(builder.buildBatch(projectSrcPath.toString(), args.getDependencyClassPath()));
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

            for (EGroumGraph example : projectExamples) {
                example.setProjectName(projectName);
                targetTypeExamples.add(example);
            }
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
        return Paths.get(getMuBenchBasePath().toString(), "checkouts/_examples/");
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

    private YamlObject getTypeUsageCounts(Collection<EGroumGraph> targets) {
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

    private Collection<AUG> loadDetectionTargets(DetectorArgs args, TargetProject targetProject) throws IOException {
        return new AUGBuilder(new DefaultAUGConfiguration() {{
            usageExamplePredicate = MisuseInstancePredicate.examplesOf(targetProject.getMisuses());
        }}).build(args.getTargetPath().srcPath, args.getDependencyClassPath());
    }

    private MuDetect createDetector(Model model) {
        return new MuDetect(
                new MinPatternActionsModel(model, 2),
                new AlternativeMappingsOverlapsFinder(new DefaultOverlapFinderConfig(new DefaultMiningConfiguration())),
                new FirstDecisionViolationPredicate(
                        new OptionalDefPrefixViolationPredicate(),
                        new MissingElementViolationPredicate()),
                new WeightRankingStrategy(
                        new ProductWeightFunction(
                                new PatternSupportWeightFunction(),
                                new PatternViolationsWeightFunction(),
                                new OverlapWithoutEdgesToMissingNodesWeightFunction(
                                        new ConstantNodeWeightFunction()
                                ))));
    }
}
