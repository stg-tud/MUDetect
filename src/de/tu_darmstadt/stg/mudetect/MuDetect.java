package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.*;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.*;

public class MuDetect {

    private final Model model;
    private final OverlapsFinder overlapsFinder;
    private final ViolationFactory violationFactory;
    private final ViolationRankingStrategy rankingStrategy;
    private final AlternativePatternInstancePredicate alternativePatternInstancePredicate;

    public MuDetect(Model model,
                    OverlapsFinder overlapsFinder,
                    ViolationFactory violationFactory,
                    ViolationRankingStrategy rankingStrategy) {
        this.model = model;
        this.overlapsFinder = overlapsFinder;
        this.violationFactory = violationFactory;
        this.rankingStrategy = rankingStrategy;
        // TODO find a testing strategy for this filtering
        this.alternativePatternInstancePredicate = new AlternativePatternInstancePredicate();
    }

    public List<Violation> findViolations(Collection<AUG> targets) {
        final Overlaps overlaps = findOverlaps(targets, model.getPatterns());
        overlaps.removeViolationIf(violation -> isAlternativePatternInstance(violation, overlaps));
        List<Violation> violations = rankingStrategy.rankViolations(overlaps, model);
        filterAlternativeViolations(violations);
        return violations;
    }

    private void filterAlternativeViolations(List<Violation> violations) {
        Set<EGroumNode> missingNodes = new HashSet<>();
        Set<EGroumEdge> missingEdges = new HashSet<>();
        Iterator<Violation> iterator = violations.iterator();
        while (iterator.hasNext()) {
            Overlap violation = iterator.next().getOverlap();
            Set<EGroumNode> mappedTargetNodes = violation.getMappedTargetNodes();
            Set<EGroumEdge> mappedTargetEdges = violation.getMappedTargetEdges();
            if (mappedTargetNodes.stream().anyMatch(missingNodes::contains) ||
                    mappedTargetEdges.stream().anyMatch(missingEdges::contains)) {
                iterator.remove();
            } else {
                missingNodes.addAll(mappedTargetNodes);
                missingEdges.addAll(mappedTargetEdges);
            }
        }
    }

    private Overlaps findOverlaps(Collection<AUG> targets, Set<Pattern> patterns) {
        Overlaps overlaps = new Overlaps();
        for (AUG target : targets) {
            for (Pattern pattern : patterns) {
                for (Overlap overlap : overlapsFinder.findOverlaps(target, pattern)) {
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

    private boolean isAlternativePatternInstance(Overlap violation, Overlaps overlaps) {
        Set<Overlap> instancesInViolationTarget = overlaps.getInstancesInSameTarget(violation);
        return alternativePatternInstancePredicate.test(violation, instancesInViolationTarget);
    }
}

