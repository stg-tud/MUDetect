package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Violation;

public interface ViolationFactory {
    boolean isViolation(Instance overlap);
}
