package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.mining.Pattern;

import java.util.Collections;
import java.util.List;

public class EmptyOverlapsFinder implements OverlapsFinder {
    private OverlapsFinder finder;

    public EmptyOverlapsFinder(OverlapsFinder finder) {
        this.finder = finder;
    }

    @Override
    public List<Overlap> findOverlaps(AUG target, Pattern pattern) {
        List<Overlap> overlaps = finder.findOverlaps(target, pattern);
        if (overlaps.isEmpty()) {
            return Collections.singletonList(
                    new Overlap(pattern, target, Collections.emptyMap(), Collections.emptyMap()));
        } else {
            return overlaps;
        }
    }
}
