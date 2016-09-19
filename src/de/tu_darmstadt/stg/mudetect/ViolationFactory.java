package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Violation;

public interface ViolationFactory {
    boolean isViolation(Instance overlap);

    Violation createViolation(Instance instance);
}
