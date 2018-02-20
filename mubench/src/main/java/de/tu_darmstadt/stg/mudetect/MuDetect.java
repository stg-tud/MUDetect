package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.*;
import edu.iastate.cs.mudetect.mining.Model;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MuDetect {

    private final Model model;
    private final OverlapsFinder overlapsFinder;
    private final ViolationPredicate violationPredicate;
    private final BiFunction<Overlaps, Model, List<Violation>> filterAndRankingStrategy;

    public MuDetect(Model model,
                    OverlapsFinder overlapsFinder,
                    ViolationPredicate violationPredicate,
                    BiFunction<Overlaps, Model, List<Violation>> filterAndRankingStrategy) {
        this.model = model;
        this.overlapsFinder = overlapsFinder;
        this.violationPredicate = violationPredicate;
        this.filterAndRankingStrategy = filterAndRankingStrategy;
    }

    public List<Violation> findViolations(Collection<APIUsageExample> targets) {
        final Overlaps overlaps = findOverlaps(targets, model.getPatterns());
        return filterAndRankingStrategy.apply(overlaps, model);
    }

    private Overlaps findOverlaps(Collection<APIUsageExample> targets, Set<APIUsagePattern> patterns) {
        Overlaps overlaps = new Overlaps();
        for (APIUsageExample target : targets) {
            for (APIUsagePattern pattern : patterns) {
                for (Overlap overlap : overlapsFinder.findOverlaps(target, pattern)) {
                    if (violationPredicate.apply(overlap).orElse(false)) {
                        overlaps.addViolation(overlap);
                    } else {
                        overlaps.addInstance(overlap);
                    }
                }
            }
        }
        return overlaps;
    }
}

