package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;

import java.util.Collection;

public class AlternativePatternInstancePredicate {
    public boolean test(Instance violation, Collection<Instance> instances) {
        for (Instance instance : instances) {
            if (violation.isSameTargetOverlap(instance)) {
                return true;
            }
        }
        return false;
    }
}
