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
        TreeSet<Violation> violations = new TreeSet<>();
        for (AUG target : targets) {
            for (Pattern pattern : model.getPatterns()) {
                Collection<Instance> overlaps = instanceFinder.findInstances(target, pattern.getAUG());
                for (Instance overlap : overlaps) {
                    if (violationFactory.isViolation(overlap)) {
                        Violation violation = violationFactory.createViolation(overlap);
                        violations.add(violation);
                    }
                }
            }
        }
        return new ArrayList<>(violations.descendingSet());
    }
}
