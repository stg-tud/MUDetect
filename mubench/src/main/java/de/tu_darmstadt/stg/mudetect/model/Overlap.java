package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import org.jgrapht.graph.DirectedSubgraph;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Overlap {

    private final DirectedSubgraph<Node, Edge> patternOverlap;
    private final DirectedSubgraph<Node, Edge> targetOverlap;
    private final Map<Node, Node> targetNodeByPatternNode = new HashMap<>();
    private final APIUsagePattern pattern;
    private final APIUsageExample target;

    public Overlap(APIUsagePattern pattern, APIUsageExample target, Map<Node, Node> targetNodeByPatternNode,
                   Map<Edge, Edge> targetEdgeByPatternEdge) {
        this.pattern = pattern;
        this.target = target;

        final Set<Node> targetNodeSet = new HashSet<>(targetNodeByPatternNode.values());
        final Set<Edge> targetEdgeSet = new HashSet<>(targetEdgeByPatternEdge.values());
        targetOverlap = new DirectedSubgraph<>(target, targetNodeSet, targetEdgeSet);

        final Set<Node> patternNodeSet = targetNodeByPatternNode.keySet();
        final Set<Edge> patternEdgeSet = targetEdgeByPatternEdge.keySet();
        patternOverlap = new DirectedSubgraph<>(pattern, patternNodeSet, patternEdgeSet);

        this.targetNodeByPatternNode.putAll(targetNodeByPatternNode);
    }

    /**
     * Clone constructor.
     */
    private Overlap(Overlap overlap) {
        this.pattern = overlap.pattern;
        this.target = overlap.target;

        this.targetOverlap = new DirectedSubgraph<>(target, overlap.targetOverlap.vertexSet(), overlap.targetOverlap.edgeSet());
        this.patternOverlap = new DirectedSubgraph<>(pattern, overlap.patternOverlap.vertexSet(), overlap.patternOverlap.edgeSet());

        this.targetNodeByPatternNode.putAll(overlap.targetNodeByPatternNode);
    }

    public APIUsagePattern getPattern() {
        return pattern;
    }

    public boolean mapsNode(Node node) {
        return patternOverlap.containsVertex(node) || targetOverlap.containsVertex(node);
    }

    public boolean mapsEdge(Edge edge) {
        return patternOverlap.containsEdge(edge) || targetOverlap.containsEdge(edge);
    }

    public Node getMappedTargetNode(Node patternNode) {
        return targetNodeByPatternNode.get(patternNode);
    }

    public APIUsageExample getTarget() { return (APIUsageExample) targetOverlap.getBase(); }

    public de.tu_darmstadt.stg.mudetect.aug.model.Location getLocation() {
        return getTarget().getLocation();
    }

    private Set<Node> getMappedPatternNodes() {
        return patternOverlap.vertexSet();
    }

    public Set<Node> getMappedTargetNodes() {
        return targetOverlap.vertexSet();
    }

    public int getNodeSize() {
        return getMappedTargetNodes().size();
    }

    public Set<Edge> getMappedTargetEdges() {
        return this.targetOverlap.edgeSet();
    }

    public int getEdgeSize() {
        return getMappedTargetEdges().size();
    }

    public int getSize() { return getNodeSize() + getEdgeSize(); }

    public Set<Node> getMissingNodes() {
        Set<Node> patternNodes = new HashSet<>(getPattern().vertexSet());
        patternNodes.removeAll(patternOverlap.vertexSet());
        return patternNodes;
    }

    public Set<Edge> getMissingEdges() {
        Set<Edge> patternEdges = new HashSet<>(getPattern().edgeSet());
        patternEdges.removeAll(patternOverlap.edgeSet());
        return patternEdges;
    }

    public boolean coversAllTargetNodesCoveredBy(Overlap other) {
        return this.getMappedTargetNodes().containsAll(other.getMappedTargetNodes());
    }

    public boolean isSameTargetOverlap(Overlap instance) {
        return this == instance || Objects.equals(targetOverlap, instance.targetOverlap);
    }

    public boolean isInTargetOverlap(Overlap other) {
        return other.getMappedTargetNodes().containsAll(this.getMappedTargetNodes()) &&
                other.getMappedTargetEdges().containsAll(this.getMappedTargetEdges());
    }

    public boolean isSamePatternOverlap(Overlap instance) {
        return this == instance || Objects.equals(patternOverlap, instance.patternOverlap);
    }

    public Overlap without(Overlap overlap) {
        Overlap reducedOverlap = new Overlap(this);
        reducedOverlap.unmapPatternNodesIf(node -> node instanceof MethodCallNode && overlap.mapsNode(node));
        if (reducedOverlap.getNodeSize() < getNodeSize()) {
            Set<Node> connectedNodes = reducedOverlap.collectNodesConnectToActions();
            reducedOverlap.unmapPatternNodesIf(node -> !connectedNodes.contains(node));
        }
        return reducedOverlap;
    }

    private void unmapPatternNodesIf(Predicate<Node> patternNodePredicate) {
        for (Node patternNode : new HashSet<>(getMappedPatternNodes())) {
            if (patternNodePredicate.test(patternNode)) {
                patternOverlap.removeVertex(patternNode);
                targetOverlap.removeVertex(targetNodeByPatternNode.get(patternNode));
                targetNodeByPatternNode.remove(patternNode);
            }
        }
    }

    private Set<Node> collectNodesConnectToActions() {
        Set<Node> connectedNodes = new HashSet<>();
        for (Node patternNode : getMappedPatternNodes()) {
            if (patternNode instanceof ActionNode) {
                collectConnectedNodes(patternNode, connectedNodes);
            }
        }
        return connectedNodes;
    }

    private void collectConnectedNodes(Node patternNode, Set<Node> connectedNodes) {
        if (!connectedNodes.contains(patternNode)) {
            connectedNodes.add(patternNode);
            Set<Node> neighbors = new HashSet<>();
            neighbors.addAll(patternOverlap.incomingEdgesOf(patternNode).stream()
                    .map(patternOverlap::getEdgeSource).collect(Collectors.toSet()));
            neighbors.addAll(patternOverlap.outgoingEdgesOf(patternNode).stream()
                    .map(patternOverlap::getEdgeTarget).collect(Collectors.toSet()));
            connectedNodes.addAll(neighbors);
            for (Node neighbor : neighbors) {
                collectConnectedNodes(neighbor, connectedNodes);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Overlap instance = (Overlap) o;
        return Objects.equals(patternOverlap, instance.patternOverlap) &&
                Objects.equals(targetOverlap, instance.targetOverlap) &&
                Objects.equals(targetNodeByPatternNode, instance.targetNodeByPatternNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patternOverlap, targetOverlap, targetNodeByPatternNode);
    }

    @Override
    public String toString() {
        return "Overlap{" +
                "patternOverlap=" + patternOverlap +
                ", targetOverlap=" + targetOverlap +
                ", targetNodeByPatternNode=" + targetNodeByPatternNode +
                '}';
    }
}
