package de.tu_darmstadt.stg.mudetect.aug.model;

import org.jgrapht.graph.DirectedMultigraph;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class APIUsageGraph extends DirectedMultigraph<Node, Edge> {
    private static final String FROZEN = "this API-usage graph is frozen";

    private boolean frozen;
    private int hash;
    private Set<Node> meaningfullActionNodesCache = null;

    public APIUsageGraph() {
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

    public Set<String> getAPIs() {
        return vertexSet().stream().map(Node::getAPI)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toSet());
    }

    /**
     * Freezes this API-usage graph. Once the graph is frozen, no elements can be added or removed from it. Trying to do
     * so will result in an {@link UnsupportedOperationException}. Once the graph is frozen, it will start caching its
     * properties, such as the hash code, to reduce performance overhead.
     */
    public void freeze() {
        this.frozen = true;
    }

    @Override
    public boolean addEdge(Node sourceVertex, Node targetVertex, Edge edge) {
        if (frozen) throw new UnsupportedOperationException(FROZEN);
        return !hasEdge(sourceVertex, edge.getClass(), targetVertex) && super.addEdge(sourceVertex, targetVertex, edge);
    }

    @Override
    public Edge addEdge(Node sourceVertex, Node targetVertex) {
        if (frozen) throw new UnsupportedOperationException(FROZEN);
        return super.addEdge(sourceVertex, targetVertex);
    }

    @Override
    public boolean addVertex(Node node) {
        if (frozen) throw new UnsupportedOperationException(FROZEN);
        return super.addVertex(node);
    }

    @Override
    protected boolean removeAllEdges(Edge[] edges) {
        if (frozen) throw new UnsupportedOperationException(FROZEN);
        return super.removeAllEdges(edges);
    }

    @Override
    public boolean removeAllEdges(Collection<? extends Edge> edges) {
        if (frozen) throw new UnsupportedOperationException(FROZEN);
        return super.removeAllEdges(edges);
    }

    @Override
    public Set<Edge> removeAllEdges(Node sourceVertex, Node targetVertex) {
        if (frozen) throw new UnsupportedOperationException(FROZEN);
        return super.removeAllEdges(sourceVertex, targetVertex);
    }

    @Override
    public boolean removeAllVertices(Collection<? extends Node> vertices) {
        if (frozen) throw new UnsupportedOperationException(FROZEN);
        return super.removeAllVertices(vertices);
    }

    @Override
    public boolean removeEdge(Edge edge) {
        if (frozen) throw new UnsupportedOperationException(FROZEN);
        return super.removeEdge(edge);
    }

    @Override
    public Edge removeEdge(Node sourceVertex, Node targetVertex) {
        if (frozen) throw new UnsupportedOperationException(FROZEN);
        return super.removeEdge(sourceVertex, targetVertex);
    }

    @Override
    public boolean removeVertex(Node node) {
        if (frozen) throw new UnsupportedOperationException(FROZEN);
        return super.removeVertex(node);
    }

    @Override
    public int hashCode() {
        if (!frozen || hash == -1) {
            hash = super.hashCode();
        }
        return hash;
    }

    private boolean hasEdge(Node edgeSource, Class<? extends Edge> edgeType, Node edgeTarget) {
        for (Edge edge : outgoingEdgesOf(edgeSource)) {
            if (edgeType.isAssignableFrom(edge.getClass()) && getEdgeTarget(edge) == edgeTarget) {
                return true;
            }
        }
        return false;
    }
}
