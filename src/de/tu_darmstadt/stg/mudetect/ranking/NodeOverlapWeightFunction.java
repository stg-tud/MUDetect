package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.Model;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public class NodeOverlapWeightFunction implements ViolationWeightFunction {
    @Override
    public float getWeight(Instance violation, Overlaps overlaps, Model model) {
        return violation.getNodeSize() / (float) violation.getPattern().getNodeSize();
    }
}
