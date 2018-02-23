package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mudetect.AlternativePatternInstancePredicate;
import de.tu_darmstadt.stg.mudetect.InstanceMethodCallPredicate;
import de.tu_darmstadt.stg.mudetect.VeryUnspecificReceiverTypePredicate;
import de.tu_darmstadt.stg.mudetect.ViolationRankingStrategy;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import edu.iastate.cs.mudetect.mining.Model;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static de.tu_darmstadt.stg.mudetect.AlternativeViolationPredicate.firstAlternativeViolation;

class DefaultFilterAndRankingStrategy implements BiFunction<Overlaps, Model, List<Violation>> {
    private final ViolationRankingStrategy rankingStrategy;
    private final AlternativePatternInstancePredicate alternativePatternInstancePredicate;

    DefaultFilterAndRankingStrategy(ViolationRankingStrategy rankingStrategy) {
        this.rankingStrategy = rankingStrategy;
        alternativePatternInstancePredicate = new AlternativePatternInstancePredicate();
    }

    @Override
    public List<Violation> apply(Overlaps overlaps, Model model) {
        overlaps.removeViolationIf(violation -> isAlternativePatternInstance(violation, overlaps));
        overlaps.mapViolations(violation -> reduceViolation(violation, overlaps));
        overlaps.removeViolationIf(this::containsNoStartNode);
        // TODO separate ranking into map to Violation with compute confidence and sort by confidence
        List<Violation> violations = rankingStrategy.rankViolations(overlaps, model);
        return violations.stream()
                .filter(firstAlternativeViolation())
                .collect(Collectors.toList());
    }

    private boolean isAlternativePatternInstance(Overlap violation, Overlaps overlaps) {
        Set<Overlap> instancesInViolationTarget = overlaps.getInstancesInSameTarget(violation);
        return alternativePatternInstancePredicate.test(violation, instancesInViolationTarget);
    }

    private Overlap reduceViolation(Overlap violation, Overlaps overlaps) {
        Set<Overlap> instancesInSameTarget = overlaps.getInstancesInSameTarget(violation);
        for (Overlap instanceInSameTarget : instancesInSameTarget) {
            violation = violation.without(instanceInSameTarget);
        }
        return violation;
    }

    private boolean containsNoStartNode(Overlap violation) {
        // SMELL We duplicate the predicate for OverlapFinder start nodes here. Can we reuse it instead?
        return violation.getMappedTargetNodes().stream()
                .noneMatch(new InstanceMethodCallPredicate().and(new VeryUnspecificReceiverTypePredicate().negate()));
    }
}
