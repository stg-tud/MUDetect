package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.aug.model.*;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import org.jgrapht.graph.DirectedSubgraph;

import java.util.*;

public class Overlap {

    private final DirectedSubgraph<Node, Edge> patternOverlap;
    private final DirectedSubgraph<Node, Edge> targetOverlap;
    private final Map<Node, Node> targetNodeByPatternNode = new HashMap<>();
    private final APIUsagePattern pattern;

    public Overlap(APIUsagePattern pattern, APIUsageExample target, Map<Node, Node> targetNodeByPatternNode,
                   Map<Edge, Edge> targetEdgeByPatternEdge) {
        this.pattern = pattern;

        final Set<Node> targetNodeSet = new HashSet<>(targetNodeByPatternNode.values());
        final Set<Edge> targetEdgeSet = new HashSet<>(targetEdgeByPatternEdge.values());
        targetOverlap = new DirectedSubgraph<>(target, targetNodeSet, targetEdgeSet);

        final Set<Node> patternNodeSet = targetNodeByPatternNode.keySet();
        final Set<Edge> patternEdgeSet = targetEdgeByPatternEdge.keySet();
        patternOverlap = new DirectedSubgraph<>(pattern, patternNodeSet, patternEdgeSet);

        this.targetNodeByPatternNode.putAll(targetNodeByPatternNode);
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

    public boolean isTargetSubgraphOf(Overlap other) {
        return other.getMappedTargetNodes().containsAll(this.getMappedTargetNodes()) &&
                other.getMappedTargetEdges().containsAll(this.getMappedTargetEdges());
    }

    public boolean coversAllTargetNodesCoveredBy(Overlap other) {
        return this.getMappedTargetNodes().containsAll(other.getMappedTargetNodes());
    }

    public boolean isSameTargetOverlap(Overlap instance) {
        return this == instance || Objects.equals(targetOverlap, instance.targetOverlap);
    }

    public boolean isSamePatternOverlap(Overlap instance) {
        return this == instance || Objects.equals(patternOverlap, instance.patternOverlap);
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
