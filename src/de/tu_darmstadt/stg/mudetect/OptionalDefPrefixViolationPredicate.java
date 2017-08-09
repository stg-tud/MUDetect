package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class OptionalDefPrefixViolationPredicate implements ViolationPredicate {
    @Override
    public Optional<Boolean> apply(Overlap overlap) {
        Set<EGroumEdge> missingDirectConnectionEdges = overlap.getMissingEdges().stream()
                .filter(EGroumEdge::isDirect)
                .filter(edge -> connectsToMappedNode(overlap, edge))
                .collect(Collectors.toSet());

        if (haveSingleMissingSource(missingDirectConnectionEdges, overlap) && haveDefiningSource(missingDirectConnectionEdges)) {
            return Optional.of(false);
        } else {
            return Optional.empty();
        }
    }

    private boolean haveDefiningSource(Set<EGroumEdge> missingDirectConnectionEdges) {
        return missingDirectConnectionEdges.stream().anyMatch(EGroumEdge::isDef);
    }

    private boolean haveSingleMissingSource(Set<EGroumEdge> missingDirectConnectionEdges, Overlap overlap) {
        Set<EGroumNode> sourceNodes = missingDirectConnectionEdges.stream()
                .map(EGroumEdge::getSource).collect(Collectors.toSet());
        return sourceNodes.size() == 1 && !overlap.mapsNode(sourceNodes.iterator().next());
    }

    private boolean connectsToMappedNode(Overlap overlap, EGroumEdge edge) {
        return overlap.mapsNode(edge.getSource()) || overlap.mapsNode(edge.getTarget());
    }
}
