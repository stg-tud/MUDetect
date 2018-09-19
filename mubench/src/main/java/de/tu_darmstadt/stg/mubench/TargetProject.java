package de.tu_darmstadt.stg.mubench;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class TargetProject {
    private final String projectId;
    private final String versionId;
    private final String[] codePath;
    private Collection<Misuse> misuses;

    static TargetProject find(Path index, String[] targetSrcPaths) throws IOException {
        try (Stream<String> lines = Files.lines(index)) {
            Map<TargetProject, List<Misuse>> collect = lines
                    .filter(line -> !line.isEmpty())
                    .map(line -> line.split("\t"))
                    .filter(line -> anyContains(targetSrcPaths, String.format("/%s/%s/", line[0], line[1])))
                    .collect(
                            Collectors.groupingBy(
                                    line -> new TargetProject(line[0], line[1], targetSrcPaths),
                                    Collectors.mapping(
                                            // TODO use all target types (line[7+])
                                            line -> new Misuse(line[2], line[4], line[5], new API(line[6])),
                                            Collectors.toList()
                                    )
                            )
                    );

            if (collect.isEmpty()) {
                throw new IllegalStateException(
                        String.format("Found no target project for paths [%s]", toString(targetSrcPaths))
                );
            }

            if (collect.size() > 1) {
                throw new IllegalStateException(
                        String.format("Found more than one target project for paths [%s]: %s",
                                toString(targetSrcPaths),
                                collect.keySet().stream().map(TargetProject::getId).collect(joining(", "))
                        ));
            }

            Map.Entry<TargetProject, List<Misuse>> data = collect.entrySet().iterator().next();
            TargetProject project = data.getKey();
            project.setMisuses(data.getValue());
            return project;
        }
    }

    private static boolean anyContains(String[] strings, String substring) {
        for (String string : strings) {
            if (string.contains(substring)) {
                return true;
            }
        }
        return false;
    }

    private static String toString(String[] strings) {
        return Arrays.stream(strings).collect(joining(", "));
    }

    private TargetProject(String projectId, String versionId, String[] codePath) {
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

    public String[] getCodePath() {
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
