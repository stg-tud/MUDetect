package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public interface ConfidenceCalculator {
    float getConfidence(Instance violation, Overlaps overlaps, Model model);
}
