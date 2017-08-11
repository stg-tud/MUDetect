package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public class RarenessWeightFunction implements ViolationWeightFunction {
    @Override
    public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
        double patternSupport = violation.getPattern().getSupport();
        double violationSupport = overlaps.getNumberOfEqualViolations(violation);
        return 1.0 - violationSupport / patternSupport;
    }

    @Override
    public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
        int patternSupport = violation.getPattern().getSupport();
        int violationSupport = overlaps.getNumberOfEqualViolations(violation);
        return String.format("1 - (%d / %d)", violationSupport, patternSupport);
    }
}
