package de.tu_darmstadt.stg.mudetect;

public interface ViolationStrategy {
    boolean isViolation(Instance overlap);
}
