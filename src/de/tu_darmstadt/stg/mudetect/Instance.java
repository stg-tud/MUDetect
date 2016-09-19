package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Condition;
import de.tu_darmstadt.stg.mudetect.model.Location;
import egroum.EGroumActionNode;
import egroum.EGroumDataNode;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.graph.DirectedSubgraph;
import org.jgrapht.graph.Subgraph;

import java.util.*;

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

    void extend(EGroumActionNode targetNode, EGroumActionNode patternNode) {
        addVertex(patternNode);

        EGroumDataNode patternReceiver = getPattern().getReceiver(patternNode);
        if (patternReceiver != null) {
            EGroumDataNode targetReceiver = target.getReceiver(targetNode);
            if (targetReceiver.getLabel().equals(patternReceiver.getLabel())) {
                addVertex(patternReceiver);
                addEdge(patternReceiver, patternNode, getPattern().getEdge(patternReceiver, patternNode));
                extend(patternReceiver, targetReceiver);
            }
        }

        Set<Condition> patternConditions = getPattern().getConditions(patternNode);
        Set<Condition> targetConditions = target.getConditions(targetNode);
        for (Condition patternCondition : patternConditions) {
            for (Condition targetCondition : targetConditions) {
                if (targetCondition.isInstanceOf(patternCondition)) {
                    EGroumActionNode patternConditionNode = patternCondition.getNode();
                    addVertex(patternConditionNode);
                    addEdge(patternConditionNode, patternNode, getPattern().getEdge(patternConditionNode, patternNode));
                    extend(targetCondition.getNode(), patternConditionNode);
                }
            }
        }

        Set<EGroumNode> patternArguments = getPattern().getArguments(patternNode);
        Set<EGroumNode> targetArguments = target.getArguments(targetNode);
        for (EGroumNode patternArgument : patternArguments) {
            for (EGroumNode targetArgument : targetArguments) {
                if (targetArgument.getLabel().equals(patternArgument.getLabel())) {
                    addVertex(patternArgument);
                    addEdge(patternArgument, patternNode, getPattern().getEdge(patternArgument, patternNode));
                    if (patternArgument instanceof EGroumDataNode) {
                        extend((EGroumDataNode) targetArgument, (EGroumDataNode) patternArgument);
                    } else {
                        extend((EGroumActionNode) targetArgument, (EGroumActionNode) patternArgument);
                    }
                }
            }
        }
    }

    private void extend(EGroumDataNode targetNode, EGroumDataNode patternNode) {
        Set<EGroumActionNode> patternInvocations = getPattern().getInvocations(patternNode);
        Set<EGroumActionNode> targetInvocations = target.getInvocations(targetNode);
        for (EGroumActionNode patternInvocation : patternInvocations) {
            if (!containsVertex(patternInvocation)) {
                for (EGroumActionNode targetInvocation : targetInvocations) {
                    if (targetInvocation.getLabel().equals(patternInvocation.getLabel())) {
                        addVertex(patternInvocation);
                        addEdge(patternNode, patternInvocation, getPattern().getEdge(patternNode, patternInvocation));
                        extend(targetInvocation, patternInvocation);
                    }
                }
            }
        }
    }

    public Location getLocation() {
        return getTarget().getLocation();
    }
}
