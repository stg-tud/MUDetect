package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.Overlap;

import java.util.List;

public interface OverlapsFinder {
    List<Overlap> findOverlaps(APIUsageExample target, APIUsagePattern pattern);
}
