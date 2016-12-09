package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Pattern;

public class MissingElementViolationFactory implements ViolationFactory {
    @Override
    public boolean isViolation(Overlap overlap) {
        Pattern pattern = overlap.getPattern();
        return overlap.getNodeSize() < pattern.getNodeSize() ||
                overlap.getEdgeSize() < pattern.getEdgeSize();
    }

}
