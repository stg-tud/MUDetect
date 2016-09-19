package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;

public class MissingElementViolationStrategy implements ViolationStrategy {
    @Override
    public boolean isViolation(Instance instance) {
        AUG pattern = instance.getPattern();
        return instance.vertexSet().size() < pattern.vertexSet().size() ||
                instance.edgeSet().size() < pattern.edgeSet().size();
    }
}
