package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.CodePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TargetProject {
    private final String projectId;
    private final String versionId;
    private final CodePath codePath;
    private Collection<Misuse> misuses;

    public static TargetProject find(Path index, CodePath targetPath) throws IOException {
        String targetSrcPath = targetPath.srcPath;
        try (Stream<String> lines = Files.lines(index)) {
            Map<TargetProject, List<Misuse>> collect = lines.map(line -> line.split("\t"))
                    .filter(line -> targetSrcPath.contains(String.format("/%s/%s/", line[0], line[1])))
                    .collect(
                            Collectors.groupingBy(
                                    line -> new TargetProject(line[0], line[1], targetPath),
                                    Collectors.mapping(
                                            line -> new Misuse(line[2], line[4], line[5], new API(line[6])),
                                            Collectors.toList()
                                    )
                            )
                    );

            if (collect.isEmpty()) {
                throw new IllegalStateException(
                        String.format("Found no target project for path '%s'", targetSrcPath)
                );
            }

            if (collect.size() > 1) {
                throw new IllegalStateException(
                        String.format("Found more than one target project for path '%s': %s",
                                targetSrcPath,
                                collect.keySet().stream().map(TargetProject::getId).collect(Collectors.joining(", "))
                        ));
            }

            Map.Entry<TargetProject, List<Misuse>> data = collect.entrySet().iterator().next();
            TargetProject project = data.getKey();
            project.setMisuses(data.getValue());
            return project;
        }
    }

    private TargetProject(String projectId, String versionId, CodePath codePath) {
        this.projectId = projectId;
        this.versionId = versionId;
        this.codePath = codePath;
    }

    public String getId() {
        return String.format("%s.%s", getProjectId(), getVersionId());
    }

    public String getProjectId() {
        return projectId;
    }

    public String getVersionId() {
        return versionId;
    }

    public CodePath getCodePath() {
        return codePath;
    }

    public Collection<Misuse> getMisuses() {
        return misuses;
    }

    private void setMisuses(Collection<Misuse> misuses) {
        this.misuses = misuses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetProject that = (TargetProject) o;
        return Objects.equals(codePath, that.codePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codePath);
    }
}
