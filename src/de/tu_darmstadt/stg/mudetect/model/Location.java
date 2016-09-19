package de.tu_darmstadt.stg.mudetect.model;

public class Location {
    private final String filePath;
    private final String methodName;

    public Location(String filePath, String methodName) {
        this.filePath = filePath;
        this.methodName = methodName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getMethodName() {
        return methodName;
    }
}
