package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public class SupportPatternWeightStrategy implements ViolationWeightStrategy {
    @Override
    public float getWeight(Instance violation, Overlaps overlaps, Model model) {
        return violation.getPattern().getSupport() / (float) model.getMaxPatternSupport();
    }
}
