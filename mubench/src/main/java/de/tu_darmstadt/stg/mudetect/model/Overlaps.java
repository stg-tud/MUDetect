package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;

import java.util.*;
import java.util.function.Predicate;

public class Overlaps {
    private Map<APIUsageExample, Set<Overlap>> instancesByTarget = new HashMap<>();
    private Map<APIUsagePattern, Set<Overlap>> violationsByPattern = new HashMap<>();
    private Map<APIUsageExample, Set<Overlap>> violationsByTarget = new HashMap<>();
    private Set<Overlap> violations = new HashSet<>();

    public Set<Overlap> getInstancesInSameTarget(Overlap overlap) {
        return instancesByTarget.getOrDefault(overlap.getTarget(), Collections.emptySet());
    }

    public Set<Overlap> getViolationsOfSamePattern(Overlap violation) {
        return violationsByPattern.get(violation.getPattern());
    }

    public Set<Overlap> getViolationsInSameTarget(Overlap overlap) {
        return violationsByTarget.get(overlap.getTarget());
    }

    public Set<Overlap> getViolations() {
        return violations;
    }

    public void addViolation(Overlap violation) {
        add(violationsByPattern, violation.getPattern(), violation);
        add(violationsByTarget, violation.getTarget(), violation);
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
