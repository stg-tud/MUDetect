package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.mining.Pattern;

import java.util.Optional;

public class MissingElementViolationPredicate implements ViolationPredicate {
    @Override
    public Optional<Boolean> isViolation(Overlap overlap) {
        return isMissingElement(overlap) ? Optional.of(true) : Optional.empty();
    }

    private boolean isMissingElement(Overlap overlap) {
        Pattern pattern = overlap.getPattern();
        return overlap.getNodeSize() < pattern.getNodeSize() || overlap.getEdgeSize() < pattern.getEdgeSize();
    }

}
