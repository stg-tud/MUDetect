package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.CodePath;
import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import egroum.AUGCollector;
import egroum.ContainsTypeUsagePredicate;
import egroum.EGroumGraph;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CrossProjectStrategy extends IntraProjectStrategy {
    @SuppressWarnings("unchecked")
    @Override
    Collection<EGroumGraph> loadTrainingExamples(DetectorArgs args) throws FileNotFoundException {
        AUGCollector collector = new AUGCollector(new DefaultAUGConfiguration());
        String targetTypeName = inferTargetType(args.getTargetPath());
        for (Object entry : getExampleData(targetTypeName)) {
            Map<String, Object> data = (Map<String, Object>) entry;
            String projectPath = (String) data.get("path");
            List<String> srcDirs = (List<String>) data.get("source_paths");
            for (String srcDir : srcDirs) {
                Path projectSrcPath = Paths.get(getMuBenchBasePath().toString(), projectPath, srcDir);
                collector.collectFrom(projectSrcPath, args.getDependencyClassPath());
            }
        }
        return collector.getAUGs().stream()
                .filter(new ContainsTypeUsagePredicate(getTargetTypeSimpleName(targetTypeName)))
                .collect(Collectors.toList());
    }

    private String inferTargetType(CodePath targetPath) {
        String targetSrcPath = targetPath.srcPath;
        try (Stream<String> lines = Files.lines(getIndexFilePath())) {
            return lines.map(Project::createProject)
                    .filter(project -> project.residesIn(targetSrcPath))
                    .map(Project::getTargetTypeName).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("failed to determine target type for " + targetSrcPath));
        } catch (IOException e) {
            throw new IllegalStateException("index file missing or corrupted", e);
        }
    }

    private Iterable<Object> getExampleData(String targetType) {
        Path dataFile = Paths.get(getExamplesBasePath().toString(), targetType + ".yml");
        try (InputStream is = new FileInputStream(dataFile.toFile())) {
            return new Yaml().loadAll(is);
        } catch (IOException e) {
            throw new IllegalArgumentException("failed to load example data for " + targetType, e);
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
        return Paths.get("/Users/svenamann/Documents/PhD/API Misuse Benchmark/MUBench", ".");
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
