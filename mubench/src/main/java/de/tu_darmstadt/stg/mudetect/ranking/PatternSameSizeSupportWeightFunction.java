package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.aug.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public class PatternSameSizeSupportWeightFunction implements ViolationWeightFunction {
    @Override
    public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
        APIUsagePattern pattern = violation.getPattern();
        return pattern.getSupport() / (double) model.getMaxPatternSupport(pattern.getNodeSize());
    }

    @Override
    public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
        APIUsagePattern pattern = violation.getPattern();
        return String.format("pattern support = %d / %d", pattern.getSupport(), model.getMaxPatternSupport(pattern.getNodeSize()));
    }
}
