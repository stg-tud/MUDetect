package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.*;

import java.util.*;

public class MuDetect {

    private final Model model;
    private final InstanceFinder instanceFinder;
    private final ViolationFactory violationFactory;

    public MuDetect(Model model, InstanceFinder instanceFinder, ViolationFactory violationFactory) {
        this.model = model;
        this.instanceFinder = instanceFinder;
        this.violationFactory = violationFactory;
    }

    public List<Violation> findViolations(Collection<AUG> targets) {
        return findViolations(findOverlaps(targets, model.getPatterns()));
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

    private List<Violation> findViolations(Overlaps overlaps) {
        PriorityQueue<Violation> violations = new PriorityQueue<>(Comparator.reverseOrder());
        for (Instance violation : overlaps.getViolations()) {
            violations.add(violationFactory.createViolation(violation));
        }
        return toList(violations);
    }

    private List<Violation> toList(PriorityQueue<Violation> violations) {
        List<Violation> result = new ArrayList<>();
        while (!violations.isEmpty()) {
            result.add(violations.poll());
        }
        return result;
    }
}

