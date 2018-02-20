package de.tu_darmstadt.stg.mudetect.ranking;

import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

/**
 * As proposed for Jadet by Wasylkowski et al. (2007).
 */
public class DefectIndicatorWeightFunction implements ViolationWeightFunction {
    private final PatternUniquenessWeightFunction patternUniquenessWeightFunction = new PatternUniquenessWeightFunction();

    @Override
    public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
        double uniqueness = patternUniquenessWeightFunction.getWeight(violation, overlaps, model);
        double patternSupport = violation.getPattern().getSupport();
        double violationSupport = overlaps.getNumberOfEqualViolations(violation);
        return uniqueness * patternSupport / violationSupport;
    }

    @Override
    public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
        String uniquenessFormula = patternUniquenessWeightFunction.getFormula(violation, overlaps, model);
        int patternSupport = violation.getPattern().getSupport();
        int violationSupport = overlaps.getNumberOfEqualViolations(violation);
        return String.format("(%s) * %d / %d", uniquenessFormula, patternSupport, violationSupport);
    }

    @Override
    public String getId() {
        return "DefectIndicator";
    }
}
