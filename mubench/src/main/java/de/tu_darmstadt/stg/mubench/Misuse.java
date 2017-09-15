package de.tu_darmstadt.stg.mubench;

public class Misuse {
    private final String id;
    private final String filePath;
    private final String methodSignature;
    private final API api;

    public Misuse(String id, String filePath, String methodSignature, API api) {
        this.id = id;
        this.filePath = filePath;
        this.methodSignature = methodSignature;
        this.api = api;
    }

    public String getId() {
        return id;
    }

    public boolean isIn(String sourceFilePath) {
        return sourceFilePath.endsWith(filePath);
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public API getMisusedAPI() {
        return api;
    }
}
