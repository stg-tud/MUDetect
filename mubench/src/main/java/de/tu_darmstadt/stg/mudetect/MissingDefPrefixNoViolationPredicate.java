package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.model.Overlap;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.DEFINITION;

public class MissingDefPrefixNoViolationPredicate implements ViolationPredicate {
    @Override
    public Optional<Boolean> apply(Overlap overlap) {
        Set<Edge> missingDirectConnectionEdges = overlap.getMissingEdges().stream()
                .filter(Edge::isDirect)
                .filter(edge -> connectsToMappedNode(overlap, edge))
                .collect(Collectors.toSet());

        if (haveSingleSource(missingDirectConnectionEdges) && haveDefiningSource(missingDirectConnectionEdges)) {
            return Optional.of(false);
        } else {
            return Optional.empty();
        }
    }

    private boolean haveDefiningSource(Set<Edge> missingDirectConnectionEdges) {
        return missingDirectConnectionEdges.stream().anyMatch(edge -> edge.getType() == DEFINITION);
    }

    private boolean haveSingleSource(Set<Edge> missingDirectConnectionEdges) {
        Set<Node> sourceNodes = missingDirectConnectionEdges.stream()
                .map(Edge::getSource).collect(Collectors.toSet());
        return sourceNodes.size() == 1;
    }

    private boolean connectsToMappedNode(Overlap overlap, Edge edge) {
        return overlap.mapsNode(edge.getSource()) || overlap.mapsNode(edge.getTarget());
    }
}
