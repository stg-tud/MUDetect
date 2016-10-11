package de.tu_darmstadt.stg.mudetect.model;

public class Pattern {
    private final AUG aug;
    private int support;

    public Pattern(AUG aug, int support) {
        this.aug = aug;
        this.support = support;
    }

    public AUG getAUG() {
        return aug;
    }

    public int getSupport() {
        return support;
    }

    public int getNodeSize() {
        return aug.vertexSet().size();
    }

    public int getEdgeSize() {
        return aug.edgeSet().size();
    }
}
