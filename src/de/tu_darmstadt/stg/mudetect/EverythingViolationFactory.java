package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;

public class EverythingViolationFactory implements ViolationFactory {
    @Override
    public boolean isViolation(Instance overlap) {
        return true;
    }
}
