package de.tu_darmstadt.stg.mudetect.overlapsfinder;

import de.tu_darmstadt.stg.mudetect.OverlapsFinder;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.Overlap;

import java.util.Collections;
import java.util.List;

public class EmptyOverlapsFinder implements OverlapsFinder {
    private OverlapsFinder finder;

    public EmptyOverlapsFinder(OverlapsFinder finder) {
        this.finder = finder;
    }

    @Override
    public List<Overlap> findOverlaps(APIUsageExample target, APIUsagePattern pattern) {
        List<Overlap> overlaps = finder.findOverlaps(target, pattern);
        if (overlaps.isEmpty()) {
            return Collections.singletonList(
                    new Overlap(pattern, target, Collections.emptyMap(), Collections.emptyMap()));
        } else {
            return overlaps;
        }
    }
}
