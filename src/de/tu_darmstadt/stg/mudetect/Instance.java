package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Equation;
import de.tu_darmstadt.stg.mudetect.model.Location;
import egroum.*;
import org.jgrapht.graph.DirectedSubgraph;

import java.util.*;
import java.util.function.Function;

public class Instance {

    private interface NodeMatcher {
        boolean match(EGroumNode targetNode, EGroumNode patternNode);
    }

    private static final NodeMatcher EQUAL_NODES =
            (targetNode, patternNode) -> targetNode.getLabel().equals(patternNode.getLabel());

    private final DirectedSubgraph<EGroumNode, EGroumEdge> patternOverlap;
    private final DirectedSubgraph<EGroumNode, EGroumEdge> targetOverlap;
    private final Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();

    /**
     * Use for testing only.
     */
    public Instance(AUG pattern, Set<EGroumNode> vertexSubset, Set<EGroumEdge> edgeSubset) {
        patternOverlap = new DirectedSubgraph<>(pattern, vertexSubset, edgeSubset);
        targetOverlap = patternOverlap;
        for (EGroumNode node : vertexSubset) {
            targetNodeByPatternNode.put(node, node);
        }
    }

    /**
     * Use for testing only.
     */
    public Instance(AUG pattern, AUG target, Map<EGroumNode, EGroumNode> targetNodeByPatternNode, Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge) {
        targetOverlap = new DirectedSubgraph<>(target, new HashSet<>(targetNodeByPatternNode.values()), new HashSet<>(targetEdgeByPatternEdge.values()));
        patternOverlap = new DirectedSubgraph<>(pattern, targetNodeByPatternNode.keySet(), targetEdgeByPatternEdge.keySet());
        this.targetNodeByPatternNode.putAll(targetNodeByPatternNode);
    }

    Instance(AUG pattern, AUG target) {
        patternOverlap = new DirectedSubgraph<>(pattern, new HashSet<>(), new HashSet<>());
        targetOverlap = new DirectedSubgraph<>(target, new HashSet<>(), new HashSet<>());
    }

    public AUG getPattern() {
        return (AUG) patternOverlap.getBase();
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

    Set<EGroumNode> getMappedTargetNodes() {
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

    void extend(EGroumNode targetNode, EGroumNode patternNode) {
        tryExtend(targetNode, patternNode);
    }

    private boolean tryExtend(EGroumNode targetNode, EGroumNode patternNode) {
        if (patternNode.isInfixOperator()) {
            Equation targetEquation = Equation.from(targetNode, getTarget());
            Equation patternEquation = Equation.from(patternNode, getPattern());
            if (!targetEquation.isInstanceOf(patternEquation)) {
                return false;
            }
        }

        map(targetNode, patternNode);

        Map<String, Set<EGroumEdge>> patternNodeInEdgesByType = getPattern().getInEdgesByType(patternNode);
        Map<String, Set<EGroumEdge>> targetNodeInEdgesByType = getTarget().getInEdgesByType(targetNode);
        for (String edgeType : patternNodeInEdgesByType.keySet()) {
            if (targetNodeInEdgesByType.containsKey(edgeType)) {
                Set<EGroumEdge> patternInEdges = patternNodeInEdgesByType.get(edgeType);
                Set<EGroumEdge> targetInEdges = targetNodeInEdgesByType.get(edgeType);
                switch (edgeType) {
                    default:
                        extendUpwards(patternInEdges, targetInEdges, EQUAL_NODES);
                }
            }
        }

        Map<String, Set<EGroumEdge>> patternNodeOutEdgesByType = getPattern().getOutEdgesByType(patternNode);
        Map<String, Set<EGroumEdge>> targetNodeOutEdgesByType = getTarget().getOutEdgesByType(targetNode);
        for (String edgeType : patternNodeOutEdgesByType.keySet()) {
            if (targetNodeOutEdgesByType.containsKey(edgeType)) {
                Set<EGroumEdge> patternOutEdges = patternNodeOutEdgesByType.get(edgeType);
                Set<EGroumEdge> targetOutEdges = targetNodeOutEdgesByType.get(edgeType);
                switch (edgeType) {
                    default:
                        extendDownwards(patternOutEdges, targetOutEdges, EQUAL_NODES);
                }
            }
        }

        return true;
    }

    private void extendUpwards(Set<EGroumEdge> patternInEdges, Set<EGroumEdge> targetInEdges, NodeMatcher matcher) {
        extend(patternInEdges, targetInEdges, matcher, EGroumEdge::getSource);
    }

    private void extendDownwards(Set<EGroumEdge> patternOutEdges, Set<EGroumEdge> targetOutEdges, NodeMatcher matcher) {
        extend(patternOutEdges, targetOutEdges, matcher, EGroumEdge::getTarget);
    }

    private void extend(Set<EGroumEdge> patternEdges, Set<EGroumEdge> targetEdges, NodeMatcher matcher,
                        Function<EGroumEdge, EGroumNode> extensionNodeSelector) {
        for (EGroumEdge patternEdge : patternEdges) {
            for (EGroumEdge targetEdge : targetEdges) {
                EGroumNode targetEdgeExtensionNode = extensionNodeSelector.apply(targetEdge);
                EGroumNode patternEdgeExtensionNode = extensionNodeSelector.apply(patternEdge);
                if (matcher.match(targetEdgeExtensionNode, patternEdgeExtensionNode)) {
                    if (isCompatibleMappingExtension(targetEdgeExtensionNode, patternEdgeExtensionNode)) {
                        if (tryExtend(targetEdgeExtensionNode, patternEdgeExtensionNode)) {
                            map(targetEdge, patternEdge);
                        }
                    } else if (isMapped(targetEdgeExtensionNode, patternEdgeExtensionNode)) {
                        map(targetEdge, patternEdge);
                    }
                }
            }
        }
    }

    private boolean isMapped(EGroumNode targetNode, EGroumNode patternNode) {
        return targetNodeByPatternNode.get(patternNode) == targetNode;
    }

    private boolean isCompatibleMappingExtension(EGroumNode targetNode, EGroumNode patterNode) {
        return !targetOverlap.vertexSet().contains(targetNode) && !patternOverlap.vertexSet().contains(patterNode);
    }

    private boolean containsPatternNode(EGroumNode node) {
        return patternOverlap.containsVertex(node);
    }

    private void map(EGroumNode targetNode, EGroumNode patternNode) {
        targetOverlap.addVertex(targetNode);
        patternOverlap.addVertex(patternNode);
        targetNodeByPatternNode.put(patternNode, targetNode);
    }

    private void map(EGroumEdge targetEdge, EGroumEdge patternEdge) {
        targetOverlap.addEdge(targetEdge.getSource(), targetEdge.getTarget(), targetEdge);
        patternOverlap.addEdge(patternEdge.getSource(), patternEdge.getTarget(), patternEdge);
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
