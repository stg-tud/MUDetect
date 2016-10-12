package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;

public class MissingElementViolationFactory implements ViolationFactory {
    @Override
    public boolean isViolation(Instance instance) {
        Pattern pattern = instance.getPattern();
        return instance.getNodeSize() < pattern.getNodeSize() ||
                instance.getEdgeSize() < pattern.getEdgeSize();
    }

}
