package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    public int getNumberOfEqualViolations(Overlap violation) {
        int numberOfEqualViolations = 0;
        for (Overlap otherViolation : getViolationsOfSamePattern(violation)) {
            // two overlaps are equal, if they violate the same pattern in the same way,
            // i.e., if the pattern overlap is the same.
            if (violation.isSamePatternOverlap(otherViolation)) {
                numberOfEqualViolations++;
            }
        }
        return numberOfEqualViolations;
    }

    public void mapViolations(Function<Overlap, Overlap> mapping) {
        Set<Overlap> violations = new HashSet<>(this.violations);
        this.violations.clear();
        this.violationsByPattern.clear();
        this.violationsByTarget.clear();
        violations.stream().map(mapping).forEach(this::addViolation);
    }
}
