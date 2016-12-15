package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.Model;
import de.tu_darmstadt.stg.mudetect.ViolationRankingStrategy;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import de.tu_darmstadt.stg.mudetect.model.Violation;

import java.util.List;
import java.util.stream.Collectors;

public class NoRankingStrategy implements ViolationRankingStrategy {

    @Override
    public List<Violation> rankViolations(Overlaps overlaps, Model model) {
        return overlaps.getViolations().stream()
                .map(violation -> new Violation(violation, 1f, "no ranking"))
                .collect(Collectors.toList());
    }
}
