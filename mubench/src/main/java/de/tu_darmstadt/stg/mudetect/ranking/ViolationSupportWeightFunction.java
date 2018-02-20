package de.tu_darmstadt.stg.mudetect.ranking;

import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public class ViolationSupportWeightFunction implements ViolationWeightFunction {
    @Override
    public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
        return 1.0 / overlaps.getNumberOfEqualViolations(violation);
    }

    @Override
    public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
        return String.format("violation support = 1 / %d", overlaps.getNumberOfEqualViolations(violation));
    }

    @Override
    public String getId() {
        return "VS";
    }
}
