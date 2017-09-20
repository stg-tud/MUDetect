package de.tu_darmstadt.stg.mudetect.aug;

import de.tu_darmstadt.stg.mudetect.aug.actions.MethodCallNode;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class APIUsageGraph extends DirectedMultigraph<Node, Edge> {
    private Set<Node> meaningfullActionNodesCache = null;

    protected APIUsageGraph() {
        super(Edge.class);
    }

    public int getNodeSize() {
        return vertexSet().size();
    }

    public int getEdgeSize() {
        return edgeSet().size();
    }

    public int getSize() {
        return getNodeSize() + getEdgeSize();
    }

    public Set<Node> incomingNodesOf(Node node) {
        return incomingEdgesOf(node).stream().map(APIUsageGraph.this::getEdgeSource).collect(Collectors.toSet());
    }

    public Set<Node> outgoingNodesOf(Node node) {
        return outgoingEdgesOf(node).stream().map(APIUsageGraph.this::getEdgeTarget).collect(Collectors.toSet());
    }

    /**
     * This declares all {@link MethodCallNode}s as meaningful. It's preferable to use an external strategy for this
     * test, such that it can be exchanged.
     */
    @Deprecated
    public Set<Node> getMeaningfulActionNodes() {
        if (meaningfullActionNodesCache == null) {
            meaningfullActionNodesCache = vertexSet().stream()
                    .filter(this::isMeaningfulAction).collect(Collectors.toSet());
        }
        return meaningfullActionNodesCache;
    }

    private boolean isMeaningfulAction(Node node) {
        return node instanceof MethodCallNode;
    }

    public Set<String> getAPIs() {
        return vertexSet().stream().map(Node::getAPI)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toSet());
    }
}
