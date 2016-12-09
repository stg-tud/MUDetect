package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;

public interface ViolationFactory {
    boolean isViolation(Overlap overlap);
}
