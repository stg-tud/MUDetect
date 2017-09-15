package egroum;

import java.util.function.Predicate;
import java.util.stream.Collectors;

import static egroum.EGroumDataEdge.Type.PARAMETER;
import static egroum.EGroumDataEdge.Type.RECEIVER;

public class DenseGroumPredicate implements Predicate<EGroumGraph> {
    private static final int MAX_BRANCHES = 100;
    private static final int MAX_REFERENCES = 9;

    public static boolean isTooDense(EGroumGraph graph) {
        return new DenseGroumPredicate().test(graph);
    }

    @Override
    public boolean test(EGroumGraph graph) {
        for (EGroumNode node : graph.getNodes()) {
            if (node instanceof EGroumDataNode) {
                if (getNumberOfOutEdges(node) < 10)
                    continue;
                if (getNumberOfOutEdges(node) > MAX_BRANCHES)
                    return true;
                if (getMaxNumberOfEqualOutEdges(node) > MAX_REFERENCES)
                    return true;
            }
        }
        return false;
    }

    private long getNumberOfOutEdges(EGroumNode node) {
        return node.outEdges.stream().filter(EGroumEdge::isDirect).count();
    }

    private long getMaxNumberOfEqualOutEdges(EGroumNode node) {
        return node.outEdges.stream().filter(this::isDirectReceiverOrParameterEdge)
                .collect(Collectors.groupingBy(EGroumEdge::toString, Collectors.counting()))
                .values().stream().mapToLong(l -> l).max().orElse(0);
    }

    private boolean isDirectReceiverOrParameterEdge(EGroumEdge edge) {
        return edge.isDirect() && edge instanceof EGroumDataEdge
                && (((EGroumDataEdge) edge).type == RECEIVER || ((EGroumDataEdge) edge).type == PARAMETER);
    }
}
