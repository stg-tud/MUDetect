package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public interface ViolationWeightFunction {
    float getWeight(Instance violation, Overlaps overlaps, Model model);
}
