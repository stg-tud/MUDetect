package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.ViolationFactory;

import java.util.*;
import java.util.function.Predicate;

public class Overlaps {
    private Map<AUG, Set<Instance>> instancesByTarget = new HashMap<>();
    private Set<Instance> violations = new HashSet<>();

    public Set<Instance> getInstancesInSameTarget(Instance overlap) {
        return instancesByTarget.getOrDefault(overlap.getTarget(), Collections.emptySet());
    }

    public Set<Instance> getViolations() {
        return violations;
    }

    public void addViolation(Instance violation) {
        violations.add(violation);
    }

    public void addInstance(Instance instance) {
        final AUG target = instance.getTarget();
        if (!instancesByTarget.containsKey(target)) {
            instancesByTarget.put(target, new HashSet<>());
        }
        instancesByTarget.get(target).add(instance);
    }

    public void removeViolationIf(Predicate<Instance> condition) {
        violations.removeIf(condition);
    }
}
