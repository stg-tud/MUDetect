package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Condition;
import de.tu_darmstadt.stg.mudetect.model.Location;
import egroum.*;
import org.jgrapht.graph.DirectedSubgraph;

import java.util.*;

import static egroum.EGroumDataEdge.Type.*;

public class Instance extends DirectedSubgraph<EGroumNode, EGroumEdge> {

    private interface NodeMatcher {
        boolean match(EGroumNode targetNode, EGroumNode patternNode);
    }

    private static final NodeMatcher EQUAL_NODES =
            (targetNode, patternNode) -> targetNode.getLabel().equals(patternNode.getLabel());

    private final AUG target;

    /**
     * Use for testing only.
     */
    public Instance(AUG pattern, Set<EGroumNode> vertexSubset, Set<EGroumEdge> edgeSubset) {
        super(pattern, vertexSubset, edgeSubset);
        this.target = pattern;
    }

    Instance(AUG pattern, AUG target) {
        super(pattern, new HashSet<>(), new HashSet<>());
        this.target = target;
    }

    public AUG getPattern() {
        return (AUG) getBase();
    }

    public AUG getTarget() { return target; }

    public Location getLocation() {
        return getTarget().getLocation();
    }

    void extend(EGroumNode targetNode, EGroumNode patternNode) {
        addVertex(patternNode);

        Map<String, Set<EGroumEdge>> patternNodeInEdgesByType = getPattern().getInEdgesByType(patternNode);
        Map<String, Set<EGroumEdge>> targetNodeInEdgesByType = getTarget().getInEdgesByType(targetNode);
        for (String edgeType : patternNodeInEdgesByType.keySet()) {
            if (targetNodeInEdgesByType.containsKey(edgeType)) {
                Set<EGroumEdge> patternInEdges = patternNodeInEdgesByType.get(edgeType);
                Set<EGroumEdge> targetInEdges = targetNodeInEdgesByType.get(edgeType);
                switch (edgeType) {
                    case "cond":
                        extendUpwards(patternInEdges, targetInEdges, (targetInNode, patternInNode) -> {
                            Condition patternCondition = getCondition(patternInNode, getPattern());
                            Condition targetCondition = getCondition(targetInNode, getTarget());
                            return targetCondition.isInstanceOf(patternCondition);
                        });
                        break;
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
    }

    private void extendUpwards(Set<EGroumEdge> patternInEdges, Set<EGroumEdge> targetInEdges, NodeMatcher matcher) {
        for (EGroumEdge patternInEdge : patternInEdges) {
            for (EGroumEdge targetInEdge : targetInEdges) {
                if (matcher.match(targetInEdge.getSource(), patternInEdge.getSource())) {
                    if (!containsVertex(patternInEdge.getSource())) {
                        extend(targetInEdge.getSource(), patternInEdge.getSource());
                    }
                    addEdge(patternInEdge);
                }
            }
        }
    }

    private void extendDownwards(Set<EGroumEdge> patternOutEdges, Set<EGroumEdge> targetOutEdges, NodeMatcher matcher) {
        for (EGroumEdge patternOutEdge : patternOutEdges) {
            for (EGroumEdge targetOutEdge : targetOutEdges) {
                if (matcher.match(targetOutEdge.getTarget(), patternOutEdge.getTarget())) {
                    if (!containsVertex(patternOutEdge.getTarget())) {
                        extend(targetOutEdge.getTarget(), patternOutEdge.getTarget());
                    }
                    addEdge(patternOutEdge);
                }
            }
        }
    }

    private void addEdge(EGroumEdge edge) {
        addEdge(edge.getSource(), edge.getTarget(), edge);
    }

    private Condition getCondition(EGroumNode conditionSource, AUG aug) {
        String conditionLabel = conditionSource.getLabel();
        if (conditionLabel.length() == 1 && EGroumNode.infixExpressionLables.values().contains(conditionLabel.charAt(0))) {
            // TODO clean the retrieval of operand arguments
            Set<EGroumEdge> operands = aug.getInEdgesByType(conditionSource).get(EGroumDataEdge.getLabel(PARAMETER));
            Iterator<EGroumEdge> iterator = operands.iterator();
            return new Condition(iterator.next().getSource(), conditionSource, iterator.next().getSource());
        } else {
            return new Condition(conditionSource);
        }
    }
}
