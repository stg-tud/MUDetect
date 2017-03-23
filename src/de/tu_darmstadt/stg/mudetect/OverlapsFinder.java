package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.mining.Pattern;

import java.util.List;

public interface OverlapsFinder {
    List<Overlap> findOverlaps(AUG target, Pattern pattern);
}
