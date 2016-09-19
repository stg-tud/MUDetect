package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Condition;
import de.tu_darmstadt.stg.mudetect.model.Location;
import egroum.*;
import org.jgrapht.graph.DirectedSubgraph;
import org.jgrapht.graph.Subgraph;

import java.util.*;

import static egroum.EGroumDataEdge.Type.*;

public class Instance extends DirectedSubgraph<EGroumNode, EGroumEdge> {

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
                        for (EGroumEdge patternInEdge : patternInEdges) {
                            for (EGroumEdge targetInEdge : targetInEdges) {
                                if (!containsVertex(patternInEdge.getSource())) {
                                    Condition patternCondition = getCondition(patternInEdge.getSource(), getPattern());
                                    Condition targetCondition = getCondition(targetInEdge.getSource(), getTarget());
                                    if (targetCondition.isInstanceOf(patternCondition)) {
                                        addVertex(patternInEdge.getSource());
                                        addEdge(patternInEdge.getSource(), patternInEdge.getTarget(), patternInEdge);
                                        extend(targetInEdge.getSource(), patternInEdge.getSource());
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        for (EGroumEdge patternInEdge : patternInEdges) {
                            for (EGroumEdge targetInEdge : targetInEdges) {
                                if (!containsVertex(patternInEdge.getSource())) {
                                    if (patternInEdge.getSource().getLabel().equals(targetInEdge.getSource().getLabel())) {
                                        addVertex(patternInEdge.getSource());
                                        addEdge(patternInEdge.getSource(), patternInEdge.getTarget(), patternInEdge);
                                        extend(targetInEdge.getSource(), patternInEdge.getSource());
                                    }
                                }
                            }
                        }
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
                        for (EGroumEdge patternOutEdge : patternOutEdges) {
                            for (EGroumEdge targetOutEdge : targetOutEdges) {
                                if (!containsVertex(patternOutEdge.getTarget())) {
                                    if (patternOutEdge.getTarget().getLabel().equals(targetOutEdge.getTarget().getLabel())) {
                                        addVertex(patternOutEdge.getTarget());
                                        addEdge(patternOutEdge.getSource(), patternOutEdge.getTarget(), patternOutEdge);
                                        extend(targetOutEdge.getTarget(), patternOutEdge.getTarget());
                                    }
                                }
                            }
                        }
                }
            }
        }
    }

    public Condition getCondition(EGroumNode conditionSource, AUG aug) {
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

    public Location getLocation() {
        return getTarget().getLocation();
    }
}
