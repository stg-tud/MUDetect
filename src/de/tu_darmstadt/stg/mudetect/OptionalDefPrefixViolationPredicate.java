package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import egroum.EGroumEdge;

import java.util.Optional;
import java.util.Set;

public class OptionalDefPrefixViolationPredicate implements ViolationPredicate {
    @Override
    public Optional<Boolean> apply(Overlap overlap) {
        Set<EGroumEdge> missingEdges = overlap.getMissingEdges();
        boolean onlyMissesDefPrefix = !missingEdges.isEmpty() && missingEdges.stream()
                .filter(EGroumEdge::isDirect).allMatch(EGroumEdge::isDef);
        return onlyMissesDefPrefix ? Optional.of(false) : Optional.empty();
    }

    private boolean mapsSourceXorTarget(Overlap overlap, EGroumEdge edge) {
        return overlap.mapsNode(edge.getSource()) ^ overlap.mapsNode(edge.getTarget());
    }
}
