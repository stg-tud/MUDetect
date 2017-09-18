package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.*;
import de.tu_darmstadt.stg.mudetect.mining.Model;

import java.util.*;

public class MuDetect {

    private final Model model;
    private final OverlapsFinder overlapsFinder;
    private final ViolationPredicate violationPredicate;
    private final ViolationRankingStrategy rankingStrategy;
    private final AlternativePatternInstancePredicate alternativePatternInstancePredicate;

    public MuDetect(Model model,
                    OverlapsFinder overlapsFinder,
                    ViolationPredicate violationPredicate,
                    ViolationRankingStrategy rankingStrategy) {
        this.model = model;
        this.overlapsFinder = overlapsFinder;
        this.violationPredicate = violationPredicate;
        this.rankingStrategy = rankingStrategy;
        // SMELL this is untested behaviour, because it's very hard to test in this context. Can we separate it?
        this.alternativePatternInstancePredicate = new AlternativePatternInstancePredicate();
    }

    public List<Violation> findViolations(Collection<APIUsageExample> targets) {
        final Overlaps overlaps = findOverlaps(targets, model.getPatterns());
        overlaps.removeViolationIf(violation -> isAlternativePatternInstance(violation, overlaps));
        return rankingStrategy.rankViolations(overlaps, model);
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

    private boolean isAlternativePatternInstance(Overlap violation, Overlaps overlaps) {
        Set<Overlap> instancesInViolationTarget = overlaps.getInstancesInSameTarget(violation);
        return alternativePatternInstancePredicate.test(violation, instancesInViolationTarget);
    }
}

