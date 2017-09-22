package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;

import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.RECEIVER;

public class DenseAUGPredicate implements Predicate<APIUsageGraph> {
    private static final int MAX_BRANCHES = 100;
    private static final int MAX_REFERENCES = 9;

    public static boolean isTooDense(APIUsageGraph graph) {
        return new DenseAUGPredicate().test(graph);
    }

    @Override
    public boolean test(APIUsageGraph graph) {
        for (Node node : graph.vertexSet()) {
            if (node instanceof DataNode) {
                if (getNumberOfOutEdges(graph, node) < 10)
                    continue;
                if (getNumberOfOutEdges(graph, node) > MAX_BRANCHES)
                    return true;
                if (getMaxNumberOfEqualOutEdges(graph, node) > MAX_REFERENCES)
                    return true;
            }
        }
        return false;
    }

    private long getNumberOfOutEdges(APIUsageGraph graph, Node node) {
        return graph.outgoingEdgesOf(node).stream().filter(Edge::isDirect).count();
    }

    private long getMaxNumberOfEqualOutEdges(APIUsageGraph graph, Node node) {
        return graph.outgoingEdgesOf(node).stream().filter(this::isDirectReceiverOrParameterEdge)
                .collect(Collectors.groupingBy(edge -> edgeAndTargetId(graph, edge), Collectors.counting()))
                .values().stream().mapToLong(l -> l).max().orElse(0);
    }

    private boolean isDirectReceiverOrParameterEdge(Edge succ) {
        return succ.isDirect() && (succ.getType() == RECEIVER || succ.getType() == PARAMETER);
    }

    private String edgeAndTargetId(APIUsageGraph graph, Edge edge) {
        return edge.getLabel() + "->" + graph.getEdgeTarget(edge).getLabel();
    }
}
