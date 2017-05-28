package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;

import java.util.Optional;

public interface ViolationPredicate {
    Optional<Boolean> isViolation(Overlap overlap);
}
