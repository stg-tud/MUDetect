package de.tu_darmstadt.stg.mudetect.model;

import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.graph.DirectedSubgraph;

import java.util.*;

public class Instance {

    private final DirectedSubgraph<EGroumNode, EGroumEdge> patternOverlap;
    private final DirectedSubgraph<EGroumNode, EGroumEdge> targetOverlap;
    private final Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
    private final Pattern pattern;

    public Instance(Pattern pattern, AUG target, Map<EGroumNode, EGroumNode> targetNodeByPatternNode,
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

    public boolean mapsPatternNode(EGroumNode patternNode) {
        return patternOverlap.containsVertex(patternNode);
    }

    public boolean mapsPatternEdge(EGroumEdge patternEdge) {
        return patternOverlap.containsEdge(patternEdge);
    }

    public AUG getTarget() { return (AUG) targetOverlap.getBase(); }

    public Location getLocation() {
        return getTarget().getLocation();
    }

    public Set<EGroumNode> getMappedTargetNodes() {
        return targetOverlap.vertexSet();
    }

    public boolean isSubInstanceOf(Instance other) {
        return other.getMappedTargetNodes().containsAll(this.getMappedTargetNodes());
    }

    public int getNodeSize() {
        return targetOverlap.vertexSet().size();
    }

    public int getEdgeSize() {
        return targetOverlap.edgeSet().size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instance instance = (Instance) o;
        return Objects.equals(patternOverlap, instance.patternOverlap) &&
                Objects.equals(targetOverlap, instance.targetOverlap) &&
                Objects.equals(targetNodeByPatternNode, instance.targetNodeByPatternNode);
    }

    public boolean isSameTargetOverlap(Instance instance) {
        return this == instance || Objects.equals(targetOverlap, instance.targetOverlap);
    }

    public boolean isSamePatternOverlap(Instance instance) {
        return this == instance || Objects.equals(patternOverlap, instance.patternOverlap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patternOverlap, targetOverlap, targetNodeByPatternNode);
    }

    @Override
    public String toString() {
        return "Instance{" +
                "patternOverlap=" + patternOverlap +
                ", targetOverlap=" + targetOverlap +
                ", targetNodeByPatternNode=" + targetNodeByPatternNode +
                '}';
    }
}
