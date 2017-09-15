package de.tu_darmstadt.stg.mudetect.model;

import egroum.EGroumEdge;
import egroum.EGroumNode;
import de.tu_darmstadt.stg.mudetect.mining.Pattern;
import org.jgrapht.graph.DirectedSubgraph;

import java.util.*;

public class Overlap {

    private final DirectedSubgraph<EGroumNode, EGroumEdge> patternOverlap;
    private final DirectedSubgraph<EGroumNode, EGroumEdge> targetOverlap;
    private final Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
    private final Pattern pattern;

    public Overlap(Pattern pattern, AUG target, Map<EGroumNode, EGroumNode> targetNodeByPatternNode,
                   Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge) {
        this.pattern = pattern;

        final Set<EGroumNode> targetNodeSet = new HashSet<>(targetNodeByPatternNode.values());
        final Set<EGroumEdge> targetEdgeSet = new HashSet<>(targetEdgeByPatternEdge.values());
        targetOverlap = new DirectedSubgraph<>(target, targetNodeSet, targetEdgeSet);

        final Set<EGroumNode> patternNodeSet = targetNodeByPatternNode.keySet();
        final Set<EGroumEdge> patternEdgeSet = targetEdgeByPatternEdge.keySet();
        patternOverlap = new DirectedSubgraph<>(pattern, patternNodeSet, patternEdgeSet);

        this.targetNodeByPatternNode.putAll(targetNodeByPatternNode);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public boolean mapsNode(EGroumNode node) {
        return patternOverlap.containsVertex(node) || targetOverlap.containsVertex(node);
    }

    public boolean mapsEdge(EGroumEdge edge) {
        return patternOverlap.containsEdge(edge) || targetOverlap.containsEdge(edge);
    }

    public EGroumNode getMappedTargetNode(EGroumNode patternNode) {
        return targetNodeByPatternNode.get(patternNode);
    }

    public AUG getTarget() { return (AUG) targetOverlap.getBase(); }

    public Location getLocation() {
        return getTarget().getLocation();
    }

    public Set<EGroumNode> getMappedTargetNodes() {
        return targetOverlap.vertexSet();
    }

    public int getNodeSize() {
        return getMappedTargetNodes().size();
    }

    public Set<EGroumEdge> getMappedTargetEdges() {
        return this.targetOverlap.edgeSet();
    }

    public int getEdgeSize() {
        return getMappedTargetEdges().size();
    }

    public int getSize() { return getNodeSize() + getEdgeSize(); }

    public Set<EGroumNode> getMissingNodes() {
        Set<EGroumNode> patternNodes = new HashSet<>(getPattern().vertexSet());
        patternNodes.removeAll(patternOverlap.vertexSet());
        return patternNodes;
    }

    public Set<EGroumEdge> getMissingEdges() {
        Set<EGroumEdge> patternEdges = new HashSet<>(getPattern().edgeSet());
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
