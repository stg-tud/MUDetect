package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;

public class EverythingViolationPredicate implements ViolationPredicate {
    @Override
    public boolean isViolation(Overlap overlap) {
        return true;
    }
}
