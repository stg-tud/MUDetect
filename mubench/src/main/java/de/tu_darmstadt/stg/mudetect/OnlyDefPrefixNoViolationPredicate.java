package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.DefinitionEdge;
import de.tu_darmstadt.stg.mudetect.model.Overlap;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class OnlyDefPrefixNoViolationPredicate implements ViolationPredicate {
    @Override
    public Optional<Boolean> apply(Overlap overlap) {
        Set<Edge> missingDirectConnectionEdges = overlap.getMissingEdges().stream()
                .filter(Edge::isDirect)
                .filter(edge -> connectsToMappedNode(edge, overlap))
                .collect(Collectors.toSet());

        if (allHaveMappedSourceAndMissingTarget(missingDirectConnectionEdges, overlap)) {
            Set<Node> missingEdgeSources = getSources(missingDirectConnectionEdges);
            if (isSingleDataNode(missingEdgeSources) || isProducerDataPair(missingEdgeSources, overlap)) {
                return Optional.of(false);
            }
        }

        return Optional.empty();
    }

    private boolean isProducerDataPair(Set<Node> missingEdgeSources, Overlap overlap) {
        if (missingEdgeSources.size() == 2) {
            Node producer = null;
            Node defined = null;

            for (Node missingEdgeSource : missingEdgeSources) {
                if (missingEdgeSource instanceof ActionNode) {
                    producer = missingEdgeSource;
                } else if (missingEdgeSource instanceof DataNode) {
                    defined = missingEdgeSource;
                }
            }

            if (producer != null && defined != null) {
                return hasMappedDefinitionEdge(overlap, producer, defined);
            }
        }
        return false;
    }

    private boolean hasMappedDefinitionEdge(Overlap overlap, Node producer, Node defined) {
        producer = overlap.getMappedTargetNode(producer);
        defined = overlap.getMappedTargetNode(defined);
        for (Edge edge : overlap.getMappedTargetEdges()) {
            if (edge.getSource() == producer && edge.getTarget() == defined && edge instanceof DefinitionEdge) {
                return true;
            }
        }
        return false;
    }

    private boolean isSingleDataNode(Set<Node> missingEdgeSources) {
        return missingEdgeSources.size() == 1 && missingEdgeSources.iterator().next() instanceof DataNode;
    }

    private Set<Node> getSources(Set<Edge> missingDirectConnectionEdges) {
        return missingDirectConnectionEdges.stream().map(Edge::getSource).collect(Collectors.toSet());
    }

    private boolean allHaveMappedSourceAndMissingTarget(Set<Edge> missingDirectConnectionEdges, Overlap overlap) {
        return missingDirectConnectionEdges.stream().allMatch(edge -> hasMappedSourceAndMissingTarget(overlap, edge));
    }

    private boolean hasMappedSourceAndMissingTarget(Overlap overlap, Edge edge) {
        return overlap.mapsNode(edge.getSource()) && !overlap.mapsNode(edge.getTarget());
    }

    private boolean connectsToMappedNode(Edge edge, Overlap overlap) {
        return overlap.mapsNode(edge.getSource()) || overlap.mapsNode(edge.getTarget());
    }
}
