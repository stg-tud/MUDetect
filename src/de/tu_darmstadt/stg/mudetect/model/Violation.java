package de.tu_darmstadt.stg.mudetect.model;

import java.util.Objects;

public class Violation implements Comparable<Violation> {

    private final Overlap overlap;
    private float confidence;

    public Violation(Overlap overlap, float confidence) {
        this.overlap = overlap;
        this.confidence = confidence;
    }

    public Overlap getOverlap() {
        return overlap;
    }

    public Location getLocation() {
        return overlap.getLocation();
    }

    public float getConfidence() {
        return confidence;
    }

    @Override
    public int compareTo(Violation o) {
        return Float.compare(getConfidence(), o.getConfidence());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Violation violation = (Violation) o;
        return Float.compare(violation.confidence, confidence) == 0 &&
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
