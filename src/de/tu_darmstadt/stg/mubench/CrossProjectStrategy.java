package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.CodePath;
import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.yaml.YamlObject;
import egroum.AUGCollector;
import egroum.EGroumGraph;
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

class CrossProjectStrategy extends IntraProjectStrategy {
    @Override
    Collection<EGroumGraph> loadTrainingExamples(DetectorArgs args, DetectorOutput.Builder output) throws FileNotFoundException {
        Collection<String> targetTypeNames = inferTargetTypes(args.getTargetPath());
        Collection<EGroumGraph> examples = new HashSet<>();
        for (String targetTypeName : targetTypeNames) {
            System.out.println(String.format("[MuDetectXProject] Target Type = %s", targetTypeName));
            String targetTypeSimpleName = getTargetTypeSimpleName(targetTypeName);
            System.out.println(String.format("[MuDetectXProject] Target Type Simple Name = %s", targetTypeSimpleName));

            List<ExampleProject> exampleProjects = getExampleProjects(targetTypeName);
            System.out.println(String.format("[MuDetectXProject] Example Projects = %d", exampleProjects.size()));

            AUGCollector collector = new AUGCollector(new DefaultAUGConfiguration() {{
                apiClasses = new String[]{targetTypeSimpleName, targetTypeName};
            }});
            for (ExampleProject exampleProject : exampleProjects) {
                for (String srcDir : exampleProject.getSrcDirs()) {
                    Path projectSrcPath = Paths.get(exampleProject.getProjectPath(), srcDir);
                    System.out.println(String.format("[MuDetectXProject] Scanning path %s", projectSrcPath));
                    collector.collectFrom(exampleProject.getProjectPath(), projectSrcPath, args.getDependencyClassPath());
                }
            }
            Collection<EGroumGraph> targetTypeExamples = collector.getAUGs();
            System.out.println(String.format("[MuDetectXProject] Examples = %d", targetTypeExamples.size()));

            output.withRunInfo("targetType-" + targetTypeName, new YamlObject() {{
                put("numberOfExampleProjects", exampleProjects.size());
                put("numberOfExamples", targetTypeExamples.size());
            }});

            examples.addAll(targetTypeExamples);
        }
        return examples;
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

    private List<ExampleProject> getExampleProjects(String targetType) {
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

    private String getTargetTypeSimpleName(String targetTypeName) {
        return targetTypeName.substring(targetTypeName.lastIndexOf('.') + 1);
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
}
