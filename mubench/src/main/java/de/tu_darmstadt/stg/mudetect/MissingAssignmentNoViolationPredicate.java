package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.AssignmentNode;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ParameterEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.Overlap;

import java.util.Optional;
import java.util.Set;

public class MissingAssignmentNoViolationPredicate implements ViolationPredicate {
    @Override
    public Optional<Boolean> apply(Overlap overlap) {
        if (missesOnlyAssignmentsAndExclusiveAssignmentParameters(overlap))
            return Optional.of(false);
        else
            return Optional.empty();
    }

    private boolean missesOnlyAssignmentsAndExclusiveAssignmentParameters(Overlap overlap) {
        Set<Node> missingNodes = overlap.getMissingNodes();
        APIUsagePattern graph = overlap.getPattern();
        return missingNodes.stream().allMatch(node -> node instanceof AssignmentNode || isExclusiveParameterOf(node, missingNodes, graph))
                && allConnectToOneOf(overlap.getMissingEdges(), missingNodes);
    }

    private boolean isExclusiveParameterOf(Node node, Set<Node> missingNodes, APIUsageGraph graph) {
        return node instanceof DataNode && graph.edgesOf(node).stream().allMatch(edge -> isParameterEdgeToOneOf(edge, node, missingNodes, graph));
    }

    private boolean isParameterEdgeToOneOf(Edge edge, Node node, Set<Node> missingNodes, APIUsageGraph graph) {
        return graph.getEdgeSource(edge) == node && edge instanceof ParameterEdge && missingNodes.contains(graph.getEdgeTarget(edge));
    }

    private boolean allConnectToOneOf(Set<Edge> missingEdges, Set<Node> missingNodes) {
        return missingEdges.stream()
                .allMatch(edge -> missingNodes.contains(edge.getTarget()) || missingNodes.contains(edge.getSource()));
    }
}
