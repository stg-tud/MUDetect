package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import de.tu_darmstadt.stg.mudetect.model.Pattern;

public class SupportConfidenceCalculator implements ConfidenceCalculator {
    private final float patternSupportWeightFactor;
    private final float overlapSizeWeightFactor;
    private final float violationSupportWeightFactor;

    public SupportConfidenceCalculator(int patternSupportWeightFactor,
                                       int overlapSizeWeightFactor,
                                       int violationSupportWeightFactor) {
        float factorSum = patternSupportWeightFactor + overlapSizeWeightFactor + violationSupportWeightFactor;
        this.patternSupportWeightFactor = patternSupportWeightFactor / factorSum;
        this.overlapSizeWeightFactor = overlapSizeWeightFactor / factorSum;
        this.violationSupportWeightFactor = violationSupportWeightFactor / factorSum;
    }

    @Override
    public float getConfidence(Instance violation, Overlaps overlaps, Model model) {
        return patternSupportWeightFactor * getPatternSupportWeight(violation, model) +
                overlapSizeWeightFactor * new NodeOverlapWeightFunction().getWeight(violation, overlaps, model) +
                violationSupportWeightFactor * new ViolationSupportWeightFunction().getWeight(violation, overlaps, model);
    }

    private float getPatternSupportWeight(Instance violation, Model model) {
        Pattern pattern = violation.getPattern();
        return pattern.getSupport() / (float) model.getMaxPatternSupport(pattern.getNodeSize());
    }
}
