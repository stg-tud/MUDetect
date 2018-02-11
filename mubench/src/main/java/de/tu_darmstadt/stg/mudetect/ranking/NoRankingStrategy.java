package de.tu_darmstadt.stg.mudetect.ranking;

import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.ViolationRankingStrategy;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import de.tu_darmstadt.stg.mudetect.model.Violation;

import java.util.List;
import java.util.stream.Collectors;

public class NoRankingStrategy implements ViolationRankingStrategy {

    @Override
    public List<Violation> rankViolations(Overlaps overlaps, Model model) {
        return overlaps.getViolations().stream()
                .map(violation -> new Violation(violation, 1.0, "no ranking"))
                .collect(Collectors.toList());
    }
}
