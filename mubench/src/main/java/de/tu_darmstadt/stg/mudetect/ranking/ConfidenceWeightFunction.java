package de.tu_darmstadt.stg.mudetect.ranking;

import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

/**
 * As proposed for DMMC by Monperrus et al. (2013).
 */
public class ConfidenceWeightFunction implements ViolationWeightFunction {
    @Override
    public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
        double patternSupport = violation.getPattern().getSupport();
        double patternViolations = overlaps.getViolationsOfSamePattern(violation).size();
        return patternSupport / (patternSupport + patternViolations);
    }

    @Override
    public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
        int patternSupport = violation.getPattern().getSupport();
        int patternViolations = overlaps.getViolationsOfSamePattern(violation).size();
        return String.format("%d / (%d + %d)", patternSupport, patternSupport, patternViolations);
    }

    @Override
    public String getId() {
        return "Confidence";
    }
}
