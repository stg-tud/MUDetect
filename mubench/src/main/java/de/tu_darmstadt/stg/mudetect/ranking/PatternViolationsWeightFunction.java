package de.tu_darmstadt.stg.mudetect.ranking;

import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public class PatternViolationsWeightFunction implements ViolationWeightFunction {
    @Override
    public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
        return 1.0 / overlaps.getViolationsOfSamePattern(violation).size();
    }

    @Override
    public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
        return String.format("pattern violations = 1 / %d", overlaps.getViolationsOfSamePattern(violation).size());
    }

    @Override
    public String getId() {
        return "PV";
    }
}
