package de.tu_darmstadt.stg.mubench;

public class API {
    private final String qualifiedName;

    public API(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getName() {
        return qualifiedName;
    }

    public String getSimpleName() {
        return qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
    }

    @Override
    public String toString() {
        return qualifiedName;
    }
}
