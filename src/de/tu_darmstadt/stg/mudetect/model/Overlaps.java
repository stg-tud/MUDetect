package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.ViolationFactory;

import java.util.*;
import java.util.function.Predicate;

public class Overlaps {
    private Map<AUG, Set<Instance>> instancesByTarget = new HashMap<>();
    private Map<Pattern, Set<Instance>> violationsByPattern = new HashMap<>();
    private Set<Instance> violations = new HashSet<>();

    public Set<Instance> getInstancesInSameTarget(Instance overlap) {
        return instancesByTarget.getOrDefault(overlap.getTarget(), Collections.emptySet());
    }

    public Set<Instance> getViolationsOfSamePattern(Instance violation) {
        return violationsByPattern.get(violation.getPattern());
    }

    public Set<Instance> getViolations() {
        return violations;
    }

    public void addViolation(Instance violation) {
        add(violationsByPattern, violation.getPattern(), violation);
        violations.add(violation);
    }

    public void addInstance(Instance instance) {
        add(instancesByTarget, instance.getTarget(), instance);
    }

    private <T> void add(Map<T, Set<Instance>> map, T key, Instance instance) {
        if (!map.containsKey(key)) {
            map.put(key, new HashSet<>());
        }
        map.get(key).add(instance);
    }

    public void removeViolationIf(Predicate<Instance> condition) {
        violations.removeIf(condition);
    }
}
