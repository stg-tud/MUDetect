package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.*;

import java.util.*;

public class MuDetect {

    private final Model model;
    private final InstanceFinder instanceFinder;
    private final ViolationStrategy violationStrategy;

    public MuDetect(Model model, InstanceFinder instanceFinder, ViolationStrategy violationStrategy) {
        this.model = model;
        this.instanceFinder = instanceFinder;
        this.violationStrategy = violationStrategy;
    }

    public List<Violation> findViolations(Collection<AUG> targets) {
        Set<Violation> violations = new HashSet<>();
        for (AUG target : targets) {
            for (Pattern pattern : model.getPatterns()) {
                Collection<Instance> overlaps = instanceFinder.findInstances(pattern.getAUG(), target);
                for (Instance overlap : overlaps) {
                    if (violationStrategy.isViolation(overlap)) {
                        violations.add(new Violation(overlap));
                    }
                }
            }
        }
        return new ArrayList<>(violations);
    }
}
