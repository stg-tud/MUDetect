package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.dot.ViolationDotExporter;

import java.util.Objects;

public class Violation implements Comparable<Violation> {

    private final Instance instance;
    private float confidence;

    public Violation(Instance overlap, float confidence) {
        this.instance = overlap;
        this.confidence = confidence;
    }

    public Instance getInstance() {
        return instance;
    }

    public Location getLocation() {
        return instance.getLocation();
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
                Objects.equals(instance, violation.instance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance, confidence);
    }

    @Override
    public String toString() {
        return "Violation{" +
                "instance=" + instance +
                ", confidence=" + confidence +
                '}';
    }
}
