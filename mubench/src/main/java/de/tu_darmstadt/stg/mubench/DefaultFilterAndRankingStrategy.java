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

public class DefaultFilterAndRankingStrategy implements BiFunction<Overlaps, Model, List<Violation>> {
    private final ViolationRankingStrategy rankingStrategy;
    private final AlternativePatternInstancePredicate alternativePatternInstancePredicate;

    public DefaultFilterAndRankingStrategy(ViolationRankingStrategy rankingStrategy) {
        this.rankingStrategy = rankingStrategy;
        alternativePatternInstancePredicate = new AlternativePatternInstancePredicate();
    }

    @Override
    public List<Violation> apply(Overlaps overlaps, Model model) {
        // TODO separate ranking into map to Violation with compute confidence and sort by confidence
        List<Violation> violations = rankingStrategy.rankViolations(overlaps, model);
        return violations.stream()
                .filter(violation -> isNotAlternativePatternInstance(violation, overlaps))
                .filter(firstAlternativeViolation())
                .collect(Collectors.toList());
    }

    private boolean isNotAlternativePatternInstance(Violation violation, Overlaps overlaps) {
        Overlap overlap = violation.getOverlap();
        Set<Overlap> instancesInViolationTarget = overlaps.getInstancesInSameTarget(overlap);
        return !alternativePatternInstancePredicate.test(overlap, instancesInViolationTarget);
    }
}
