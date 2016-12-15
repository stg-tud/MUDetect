package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public class ViolationSupportWeightFunction implements ViolationWeightFunction {
    @Override
    public float getWeight(Overlap violation, Overlaps overlaps, Model model) {
        return 1f / getNumberOfEqualViolations(violation, overlaps);
    }

    @Override
    public String toString(Overlap violation, Overlaps overlaps, Model model) {
        return String.format("violation support = 1 / %d", getNumberOfEqualViolations(violation, overlaps));
    }

    private int getNumberOfEqualViolations(Overlap violation, Overlaps overlaps) {
        int numberOfEqualViolations = 0;
        for (Overlap otherViolation : overlaps.getViolationsOfSamePattern(violation)) {
            // two overlaps are equal, if they violate the same pattern in the same way,
            // i.e., if the pattern overlap is the same.
            if (violation.isSamePatternOverlap(otherViolation)) {
                numberOfEqualViolations++;
            }
        }
        return numberOfEqualViolations;
    }
}
