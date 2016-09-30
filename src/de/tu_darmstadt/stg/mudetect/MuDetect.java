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
        return findViolations(findInstances(targets));
    }

    private Set<Instance> findInstances(Collection<AUG> targets) {
        Set<Instance> overlaps = new HashSet<>();
        for (AUG target : targets) {
            for (Pattern pattern : model.getPatterns()) {
                overlaps.addAll(instanceFinder.findInstances(target, pattern.getAUG()));
            }
        }
        return overlaps;
    }

    private List<Violation> findViolations(Set<Instance> overlaps) {
        PriorityQueue<Violation> violations = new PriorityQueue<>(Comparator.reverseOrder());
        for (Instance overlap : overlaps) {
            if (violationFactory.isViolation(overlap)) {
                Violation violation = violationFactory.createViolation(overlap);
                violations.add(violation);
            }
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
