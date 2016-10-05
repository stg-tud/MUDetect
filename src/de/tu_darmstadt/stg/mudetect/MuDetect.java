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
        return findViolations(findInstances(targets, model.getPatterns()));
    }

    private Instances findInstances(Collection<AUG> targets, Collection<Pattern> patterns) {
        Instances instances = new Instances();
        for (AUG target : targets) {
            for (Pattern pattern : patterns) {
                instances.addAll(instanceFinder.findInstances(target, pattern.getAUG()));
            }
        }
        return instances;
    }

    private List<Violation> findViolations(Instances instances) {
        PriorityQueue<Violation> violations = new PriorityQueue<>(Comparator.reverseOrder());
        for (Instance instance : instances) {
            if (violationFactory.isViolation(instance)) {
                Violation violation = violationFactory.createViolation(instance);
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

