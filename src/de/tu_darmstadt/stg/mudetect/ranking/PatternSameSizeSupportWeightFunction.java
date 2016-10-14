package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.Model;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import de.tu_darmstadt.stg.mudetect.model.Pattern;

public class PatternSameSizeSupportWeightFunction implements ViolationWeightFunction {
    @Override
    public float getWeight(Instance violation, Overlaps overlaps, Model model) {
        Pattern pattern = violation.getPattern();
        return pattern.getSupport() / (float) model.getMaxPatternSupport(pattern.getNodeSize());
    }
}
