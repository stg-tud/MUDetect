package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Violation;

public class MissingElementViolationFactory implements ViolationFactory {
    @Override
    public boolean isViolation(Instance instance) {
        AUG pattern = instance.getPattern();
        return instance.vertexSet().size() < pattern.vertexSet().size() ||
                instance.edgeSet().size() < pattern.edgeSet().size();
    }

    @Override
    public Violation createViolation(Instance instance) {
        return new Violation(instance);
    }
}
