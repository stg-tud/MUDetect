package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;

import java.util.Optional;
import java.util.function.Function;

public class FirstDecisionViolationPredicate implements ViolationPredicate {
    private final Function<Overlap, Optional<Boolean>>[] predicates;

    @SafeVarargs
    public FirstDecisionViolationPredicate(Function<Overlap, Optional<Boolean>>... predicates) {
        this.predicates = predicates;
    }

    @Override
    public Optional<Boolean> apply(Overlap overlap) {
        for (Function<Overlap, Optional<Boolean>> predicate : predicates) {
            Optional<Boolean> decision = predicate.apply(overlap);
            if (decision.isPresent())
                return decision;
        }
        return Optional.empty();
    }
}
