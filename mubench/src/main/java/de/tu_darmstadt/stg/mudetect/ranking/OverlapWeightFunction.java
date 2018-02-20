package de.tu_darmstadt.stg.mudetect.ranking;

import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public class OverlapWeightFunction implements ViolationWeightFunction {
    @Override
    public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
        int violationSize = violation.getSize();
        int patternSize = violation.getPattern().getSize();
        return violationSize / (float) patternSize;
    }

    public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
        int violationSize = violation.getSize();
        int patternSize = violation.getPattern().getSize();
        return String.format("overlap = %d / %d", violationSize, patternSize);
    }

    @Override
    public String getId() {
        return "O";
    }
}
