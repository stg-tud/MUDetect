package de.tu_darmstadt.stg.mudetect.model;

import java.util.Objects;

public class Violation implements Comparable<Violation> {

    private final Overlap overlap;
    private final double confidence;
    private final String confidenceString;

    public Violation(Overlap overlap, double confidence, String confidenceString) {
        this.overlap = overlap;
        this.confidence = confidence;
        this.confidenceString = confidenceString;
    }

    public Overlap getOverlap() {
        return overlap;
    }

    public de.tu_darmstadt.stg.mudetect.aug.model.Location getLocation() {
        return overlap.getLocation();
    }

    public double getConfidence() {
        return confidence;
    }

    public String getConfidenceString() {
        return confidenceString;
    }

    @Override
    public int compareTo(Violation o) {
        return Double.compare(getConfidence(), o.getConfidence());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Violation violation = (Violation) o;
        return Double.compare(violation.confidence, confidence) == 0 &&
                Objects.equals(overlap, violation.overlap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(overlap, confidence);
    }

    @Override
    public String toString() {
        return "Violation{" +
                "overlap=" + overlap +
                ", confidence=" + confidence +
                '}';
    }
}
