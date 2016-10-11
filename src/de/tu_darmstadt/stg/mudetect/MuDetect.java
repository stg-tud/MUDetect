package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.*;

import java.util.*;

public class MuDetect {

    private final Model model;
    private final InstanceFinder instanceFinder;
    private final ViolationFactory violationFactory;
    private final ViolationRankingStrategy rankingStrategy;

    public MuDetect(Model model,
                    InstanceFinder instanceFinder,
                    ViolationFactory violationFactory,
                    ViolationRankingStrategy rankingStrategy) {
        this.model = model;
        this.instanceFinder = instanceFinder;
        this.violationFactory = violationFactory;
        this.rankingStrategy = rankingStrategy;
    }

    public List<Violation> findViolations(Collection<AUG> targets) {
        final Overlaps overlaps = findOverlaps(targets, model.getPatterns());
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
}

