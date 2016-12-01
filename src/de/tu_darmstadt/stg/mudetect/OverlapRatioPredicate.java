package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;

import java.util.function.Predicate;

public class OverlapRatioPredicate implements Predicate<Overlap> {
    private final double overlapRatioThreshold;

    public OverlapRatioPredicate(double overlapRatioThreshold) {
        this.overlapRatioThreshold = overlapRatioThreshold;
    }

    @Override
    public boolean test(Overlap overlap) {
        return overlap.getNodeSize() / (float) overlap.getPattern().getNodeSize() > overlapRatioThreshold;
    }
}
