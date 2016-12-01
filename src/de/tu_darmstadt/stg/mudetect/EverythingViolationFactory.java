package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;

public class EverythingViolationFactory implements ViolationFactory {
    @Override
    public boolean isViolation(Overlap overlap) {
        return true;
    }
}
