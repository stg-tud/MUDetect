package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Equation;
import de.tu_darmstadt.stg.mudetect.model.Location;
import egroum.*;
import org.jgrapht.graph.DirectedSubgraph;

import java.util.*;

import static egroum.EGroumDataEdge.Type.*;

public class Instance {

    private interface NodeMatcher {
        boolean match(EGroumNode targetNode, EGroumNode patternNode);
    }

    private static final NodeMatcher EQUAL_NODES =
            (targetNode, patternNode) -> targetNode.getLabel().equals(patternNode.getLabel());

    private final DirectedSubgraph<EGroumNode, EGroumEdge> patternOverlap;
    private final DirectedSubgraph<EGroumNode, EGroumEdge> targetOverlap;

    /**
     * Use for testing only.
     */
    public Instance(AUG pattern, Set<EGroumNode> vertexSubset, Set<EGroumEdge> edgeSubset) {
        patternOverlap = new DirectedSubgraph<>(pattern, vertexSubset, edgeSubset);
        targetOverlap = patternOverlap;
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
        if (isEquationOperator(patternNode)) {
            Equation targetEquation = getEquation(targetNode, getTarget());
            Equation patternEquation = getEquation(patternNode, getPattern());
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

    private boolean isEquationOperator(EGroumNode node) {
        String conditionLabel = node.getLabel();
        return conditionLabel.length() == 1 &&
                EGroumNode.infixExpressionLables.values().contains(conditionLabel.charAt(0));
    }

    private Equation getEquation(EGroumNode operatorNode, AUG aug) {
        // TODO clean the retrieval of operand arguments
        Set<EGroumEdge> operands = aug.getInEdgesByType(operatorNode).get(EGroumDataEdge.getLabel(PARAMETER));
        Iterator<EGroumEdge> iterator = operands.iterator();
        return new Equation(iterator.next().getSource(), operatorNode, iterator.next().getSource());
    }

    private void extendUpwards(Set<EGroumEdge> patternInEdges, Set<EGroumEdge> targetInEdges, NodeMatcher matcher) {
        for (EGroumEdge patternInEdge : patternInEdges) {
            for (EGroumEdge targetInEdge : targetInEdges) {
                if (matcher.match(targetInEdge.getSource(), patternInEdge.getSource())) {
                    if (!containsPatternNode(patternInEdge.getSource())) {
                        if (tryExtend(targetInEdge.getSource(), patternInEdge.getSource())) {
                            map(targetInEdge, patternInEdge);
                        }
                    } else {
                        map(targetInEdge, patternInEdge);
                    }
                }
            }
        }
    }

    private void extendDownwards(Set<EGroumEdge> patternOutEdges, Set<EGroumEdge> targetOutEdges, NodeMatcher matcher) {
        for (EGroumEdge patternOutEdge : patternOutEdges) {
            for (EGroumEdge targetOutEdge : targetOutEdges) {
                if (matcher.match(targetOutEdge.getTarget(), patternOutEdge.getTarget())) {
                    if (!containsPatternNode(patternOutEdge.getTarget())) {
                        if (tryExtend(targetOutEdge.getTarget(), patternOutEdge.getTarget())) {
                            map(targetOutEdge, patternOutEdge);
                        }
                    } else {
                        map(targetOutEdge, patternOutEdge);
                    }
                }
            }
        }
    }

    private boolean containsPatternNode(EGroumNode node) {
        return patternOverlap.containsVertex(node);
    }

    private void map(EGroumNode targetNode, EGroumNode patternNode) {
        targetOverlap.addVertex(targetNode);
        patternOverlap.addVertex(patternNode);
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
                Objects.equals(targetOverlap, instance.targetOverlap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patternOverlap, targetOverlap);
    }

    @Override
    public String toString() {
        return patternOverlap.toString();
    }
}
