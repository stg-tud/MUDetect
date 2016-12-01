package de.tu_darmstadt.stg.mudetect.model;

import java.util.*;
import java.util.function.Predicate;

public class Overlaps {
    private Map<AUG, Set<Overlap>> instancesByTarget = new HashMap<>();
    private Map<Pattern, Set<Overlap>> violationsByPattern = new HashMap<>();
    private Set<Overlap> violations = new HashSet<>();

    public Set<Overlap> getInstancesInSameTarget(Overlap overlap) {
        return instancesByTarget.getOrDefault(overlap.getTarget(), Collections.emptySet());
    }

    public Set<Overlap> getViolationsOfSamePattern(Overlap violation) {
        return violationsByPattern.get(violation.getPattern());
    }

    public Set<Overlap> getViolations() {
        return violations;
    }

    public void addViolation(Overlap violation) {
        add(violationsByPattern, violation.getPattern(), violation);
        violations.add(violation);
    }

    public void addInstance(Overlap instance) {
        add(instancesByTarget, instance.getTarget(), instance);
    }

    private <T> void add(Map<T, Set<Overlap>> map, T key, Overlap instance) {
        if (!map.containsKey(key)) {
            map.put(key, new HashSet<>());
        }
        map.get(key).add(instance);
    }

    public void removeViolationIf(Predicate<Overlap> condition) {
        violations.removeIf(condition);
    }
}
