package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.*;

import java.util.*;

public class MuDetect {

    private final Model model;
    private final OverlapsFinder overlapsFinder;
    private final ViolationFactory violationFactory;
    private final ViolationRankingStrategy rankingStrategy;
    private final AlternativePatternInstancePredicate alternativePatternInstancePredicate;

    public MuDetect(Model model,
                    OverlapsFinder overlapsFinder,
                    ViolationFactory violationFactory,
                    ViolationRankingStrategy rankingStrategy) {
        this.model = model;
        this.overlapsFinder = overlapsFinder;
        this.violationFactory = violationFactory;
        this.rankingStrategy = rankingStrategy;
        // TODO find a testing strategy for this filtering
        this.alternativePatternInstancePredicate = new AlternativePatternInstancePredicate();
    }

    public List<Violation> findViolations(Collection<AUG> targets) {
        final Overlaps overlaps = findOverlaps(targets, model.getPatterns());
        overlaps.removeViolationIf(violation -> isAlternativePatternInstance(violation, overlaps));
        return rankingStrategy.rankViolations(overlaps, model);
    }

    private Overlaps findOverlaps(Collection<AUG> targets, Set<Pattern> patterns) {
        Overlaps overlaps = new Overlaps();
        for (AUG target : targets) {
            for (Pattern pattern : patterns) {
                for (Overlap overlap : overlapsFinder.findOverlaps(target, pattern)) {
                    if (violationFactory.isViolation(overlap)) {
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

