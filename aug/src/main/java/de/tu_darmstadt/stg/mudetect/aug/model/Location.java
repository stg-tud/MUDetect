package de.tu_darmstadt.stg.mudetect.aug.model;

import java.io.Serializable;
import java.util.Objects;

public final class Location implements Serializable {
    private final String projectName;
    private final String filePath;
    private final String methodSignature;

    public Location(String projectName, String filePath, String methodSignature) {
        this.projectName = projectName;
        this.filePath = filePath;
        this.methodSignature = methodSignature;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(projectName, location.projectName) &&
                Objects.equals(filePath, location.filePath) &&
                Objects.equals(methodSignature, location.methodSignature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectName, filePath, methodSignature);
    }
}
