package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public class PatternViolationsWeightFunction implements ViolationWeightFunction {
    @Override
    public float getWeight(Overlap violation, Overlaps overlaps, Model model) {
        return 1f / overlaps.getViolationsOfSamePattern(violation).size();
    }

    @Override
    public String toString(Overlap violation, Overlaps overlaps, Model model) {
        return String.format("pattern violations = 1 / %d", overlaps.getViolationsOfSamePattern(violation).size());
    }
}
