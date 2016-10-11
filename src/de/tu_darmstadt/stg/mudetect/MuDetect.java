package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.*;

import java.util.*;

public class MuDetect {

    private final Model model;
    private final InstanceFinder instanceFinder;
    private final ViolationFactory violationFactory;
    private final ViolationRankingStrategy rankingStrategy;
    private final AlternativePatternInstancePredicate alternativePatternInstancePredicate;

    public MuDetect(Model model,
                    InstanceFinder instanceFinder,
                    ViolationFactory violationFactory,
                    ViolationRankingStrategy rankingStrategy) {
        this.model = model;
        this.instanceFinder = instanceFinder;
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
                for (Instance overlap : instanceFinder.findInstances(target, pattern)) {
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

    private boolean isAlternativePatternInstance(Instance violation, Overlaps overlaps) {
        Set<Instance> instancesInViolationTarget = overlaps.getInstancesInSameTarget(violation);
        return alternativePatternInstancePredicate.test(violation, instancesInViolationTarget);
    }
}

