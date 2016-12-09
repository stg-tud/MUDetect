package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public class ViolationSupportWeightFunction implements ViolationWeightFunction {
    @Override
    public float getWeight(Overlap violation, Overlaps overlaps, Model model) {
        float numberOfEqualViolations = 0;
        for (Overlap otherViolation : overlaps.getViolationsOfSamePattern(violation)) {
            // two overlaps are equal, if they violate the same aPatternBuilder in the same way,
            // i.e., if the aPatternBuilder overlap is the same.
            if (violation.isSamePatternOverlap(otherViolation)) {
                numberOfEqualViolations++;
            }
        }
        return 1 / numberOfEqualViolations;
    }
}
