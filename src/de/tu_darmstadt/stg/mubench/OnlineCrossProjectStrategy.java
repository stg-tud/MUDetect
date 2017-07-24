package de.tu_darmstadt.stg.mubench;

import com.google.common.collect.Multiset;
import de.tu_darmstadt.stg.mubench.cli.CodePath;
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
import egroum.AUGCollector;
import egroum.EGroumGraph;
import mining.MethodSignatureExamplePredicate;
import mining.MethodUsageExamplePredicate;
import mining.TypeUsageExamplePredicate;
import mining.UsageExamplePredicate;
import utils.FileIO;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class OnlineCrossProjectStrategy implements DetectionStrategy {
    @Override
    public DetectorOutput detectViolations(DetectorArgs args) throws Exception {
        DetectorOutput.Builder output = createOutput();
        List<Violation> allViolations = new ArrayList<>();

        long startDetectionLoadTime = System.currentTimeMillis();
        Collection<AUG> targets = loadDetectionTargets(args);
        long endDetectionLoadTime = System.currentTimeMillis();
        output.withRunInfo("detectionLoadTime", endDetectionLoadTime - startDetectionLoadTime);
        output.withRunInfo("numberOfTargets", targets.size());
        
        for (AUG target : targets) {
        	Set<Pattern> patterns = new HashSet<>();
            Collection<String> targetTypeNames = inferTargetTypes(args.getTargetPath());
            for (String targetTypeName : targetTypeNames) {
                Type targetType = new Type(targetTypeName);
                System.out.println(String.format("[MuDetectXProject] Target Type = %s", targetType));

                long startTime = System.currentTimeMillis();
                Collection<EGroumGraph> trainingExamples = loadTrainingExamples(targetType, target, args, output);
                long endTrainingLoadTime = System.currentTimeMillis();
                output.withRunInfo(targetType + "-trainingLoadTime", endTrainingLoadTime - startTime);
                output.withRunInfo(targetType + "-numberOfTrainingExamples", trainingExamples.size());
                output.withRunInfo(targetType + "-numberOfUsagesInTrainingExamples", getTypeUsageCounts(trainingExamples));

                Model model = createMiner().mine(trainingExamples);
                long endTrainingTime = System.currentTimeMillis();
                output.withRunInfo(targetType + "-trainingTime", endTrainingTime - endTrainingLoadTime);
                output.withRunInfo(targetType + "-numberOfPatterns", model.getPatterns().size());
                output.withRunInfo(targetType + "-maxPatternSupport", model.getMaxPatternSupport());

                patterns.addAll(model.getPatterns());
            }

            Model model = () -> patterns;
            List<Violation> violations = createDetector(model).findViolations(Arrays.asList(target));
            long endDetectionTime = System.currentTimeMillis();
            output.withRunInfo("detectionTime", endDetectionTime - endDetectionLoadTime);
            output.withRunInfo("numberOfViolations", violations.size());
            output.withRunInfo("numberOfExploredAlternatives", AlternativeMappingsOverlapsFinder.numberOfExploredAlternatives);
            allViolations.addAll(violations);
        }

        return output.withFindings(allViolations, ViolationUtils::toFinding);
    }

    private class Type {
        private final String typeName;

        Type(String typeName) {
            this.typeName = typeName;
        }

        String getName() {
            return typeName;
        }

        String getSimpleName() {
            return typeName.substring(typeName.lastIndexOf('.') + 1);
        }

        @Override
        public String toString() {
            return typeName;
        }
    }

    private Collection<EGroumGraph> loadTrainingExamples(Type targetType, AUG target, DetectorArgs args, DetectorOutput.Builder output) throws FileNotFoundException {
        List<ExampleProject> exampleProjects = getExampleProjects(targetType);
        System.out.println(String.format("[MuDetectXProject] Example Projects = %d", exampleProjects.size()));
        output.withRunInfo(targetType + "-exampleProjects", exampleProjects.size());

        AUGCollector collector = new AUGCollector(new DefaultAUGConfiguration() {{
            usageExamplePredicate = TypeUsageExamplePredicate.usageExamplesOf(targetType.getName());
        }});
        for (ExampleProject exampleProject : exampleProjects) {
            for (String srcDir : exampleProject.getSrcDirs()) {
                try {
                    Path projectSrcPath = Paths.get(exampleProject.getProjectPath(), srcDir);
                    System.out.println(String.format("[MuDetectXProject] Scanning path %s", projectSrcPath));
                    collector.collectFrom(exampleProject.getProjectPath(), projectSrcPath, args.getDependencyClassPath());
                } catch (Exception e) {
                    System.err.println("[MuDetectXProject] Parsing failed.");
                    e.printStackTrace(System.err);
                }
            }
            if (collector.getAUGs().size() > 5000) {
                break;
            }
        }
        Collection<EGroumGraph> targetExamples = collector.getAUGs();
        UsageExamplePredicate usageExamplePredicate = MethodUsageExamplePredicate.usageExamplesOf(target);
        for (EGroumGraph g : targetExamples)
        	if (!usageExamplePredicate.matches(g))
        		targetExamples.remove(g);
        System.out.println(String.format("[MuDetectXProject] Examples = %d", targetExamples.size()));

        return targetExamples;
    }

    private Collection<String> inferTargetTypes(CodePath targetPath) {
        String targetSrcPath = targetPath.srcPath;
        try (Stream<String> lines = Files.lines(getIndexFilePath())) {
            Set<String> targetTypes = lines.map(Project::createProject)
                    .filter(project -> project.residesIn(targetSrcPath))
                    .map(Project::getTargetTypeName).collect(Collectors.toSet());
            if (targetTypes.isEmpty()) {
                throw new IllegalArgumentException("failed to determine target type for " + targetSrcPath);
            }
            return targetTypes;
        } catch (IOException e) {
            throw new IllegalStateException("index file missing or corrupted", e);
        }
    }

    private List<ExampleProject> getExampleProjects(Type targetType) {
        Path dataFile = Paths.get(getExamplesBasePath().toString(), targetType + ".yml");
        try (InputStream is = new FileInputStream(dataFile.toFile())) {
            return StreamSupport.stream(new Yaml().loadAll(is).spliterator(), false)
                    .map(ExampleProject::create).collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalArgumentException("failed to load example data for " + targetType, e);
        }
    }

    private static class ExampleProject {
        private final String projectPath;
        private final List<String> srcDirs;

        private ExampleProject(String projectPath, List<String> srcDirs) {
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

    private Path getIndexFilePath() {
        return Paths.get(getExamplesBasePath().toString(), "index.csv");
    }

    private Path getExamplesBasePath() {
        return Paths.get(getMuBenchBasePath().toString(), "checkouts/_examples/");
    }

    private Path getMuBenchBasePath() {
        return Paths.get(".");
    }

    private static class Project {
        private final String projectId;
        private final String versionId;
        private final String targetTypeName;

        private Project(String projectId, String versionId, String targetTypeName) {
            this.projectId = projectId;
            this.versionId = versionId;
            this.targetTypeName = targetTypeName;
        }

        static Project createProject(String line) {
            String[] info = line.split("\t");
            return new Project(info[0], info[1], info[2]);
        }

        String getProjectId() {
            return projectId;
        }

        String getVersionId() {
            return versionId;
        }

        String getTargetTypeName() {
            return targetTypeName;
        }

        boolean residesIn(String targetSrcPath) {
            return targetSrcPath.contains(String.format("/%s/%s/", getProjectId(), getVersionId()));
        }
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
            minPatternSupport = 10;
        }});
    }

    private Collection<AUG> loadDetectionTargets(DetectorArgs args) throws IOException {
    	String[] signatures = getTargetMethodsWithMisuses(args);
        return new AUGBuilder(
        			new DefaultAUGConfiguration(){
        				{usageExamplePredicate = MethodSignatureExamplePredicate.usageExamplesOf(signatures);}
        			}
        		).build(args.getTargetPath().srcPath, args.getDependencyClassPath());
    }

    private String[] getTargetMethodsWithMisuses(DetectorArgs args) {
    	Set<String> signatures = new HashSet<>();
    	String content = FileIO.readStringFromFile("checkouts/_examples/index_methods.csv");
    	Scanner sc = new Scanner(content);
    	while (sc.hasNextLine()) {
    		String line = sc.nextLine();
    		String[] parts = line.split("\t");
    		signatures.add(parts[1] + "/" + parts[4] + "/" + parts[5]);
    	}
    	sc.close();
		return signatures.toArray(new String[0]);
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
