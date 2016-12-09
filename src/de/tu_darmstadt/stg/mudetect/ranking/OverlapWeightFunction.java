package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import de.tu_darmstadt.stg.mudetect.model.Pattern;

public class OverlapWeightFunction implements ViolationWeightFunction {
    @Override
    public float getWeight(Overlap violation, Overlaps overlaps, Model model) {
        int violationSize = violation.getNodeSize() + violation.getEdgeSize();
        Pattern pattern = violation.getPattern();
        int patternSize = pattern.getNodeSize() + pattern.getEdgeSize();
        return violationSize / (float) patternSize;
    }
}
