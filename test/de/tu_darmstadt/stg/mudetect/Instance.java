package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Condition;
import egroum.EGroumActionNode;
import egroum.EGroumDataNode;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.graph.Subgraph;

import java.util.*;

public class Instance extends Subgraph<EGroumNode, EGroumEdge, AUG> {

    private final AUG pattern;
    private final AUG target;

    /**
     * Use for testing only.
     */
    public Instance(AUG pattern, Set<EGroumNode> vertexSubset, Set<EGroumEdge> edgeSubset) {
        super(pattern, vertexSubset, edgeSubset);
        this.pattern = pattern;
        this.target = pattern;
    }

    Instance(AUG pattern, AUG target) {
        super(pattern, new HashSet<>());
        this.pattern = pattern;
        this.target = target;
    }

    void extend(EGroumActionNode targetNode, EGroumActionNode patternNode) {
        addVertex(patternNode);

        EGroumDataNode patternReceiver = pattern.getReceiver(patternNode);
        if (patternReceiver != null) {
            EGroumDataNode targetReceiver = target.getReceiver(targetNode);
            if (targetReceiver.getLabel().equals(patternReceiver.getLabel())) {
                addVertex(patternReceiver);
                addEdge(patternReceiver, patternNode, pattern.getEdge(patternReceiver, patternNode));
                extend(patternReceiver, targetReceiver);
            }
        }

        Set<Condition> patternConditions = pattern.getConditions(patternNode);
        Set<Condition> targetConditions = target.getConditions(targetNode);
        for (Condition patternCondition : patternConditions) {
            for (Condition targetCondition : targetConditions) {
                if (targetCondition.isInstanceOf(patternCondition)) {
                    EGroumActionNode patternConditionNode = patternCondition.getNode();
                    addVertex(patternConditionNode);
                    addEdge(patternConditionNode, patternNode, pattern.getEdge(patternConditionNode, patternNode));
                    extend(targetCondition.getNode(), patternConditionNode);
                }
            }
        }

        Set<EGroumNode> patternArguments = pattern.getArguments(patternNode);
        Set<EGroumNode> targetArguments = target.getArguments(targetNode);
        for (EGroumNode patternArgument : patternArguments) {
            for (EGroumNode targetArgument : targetArguments) {
                if (targetArgument.getLabel().equals(patternArgument.getLabel())) {
                    addVertex(patternArgument);
                    addEdge(patternArgument, patternNode, pattern.getEdge(patternArgument, patternNode));
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
        Set<EGroumActionNode> patternInvocations = pattern.getInvocations(patternNode);
        Set<EGroumActionNode> targetInvocations = target.getInvocations(targetNode);
        for (EGroumActionNode patternInvocation : patternInvocations) {
            if (!containsVertex(patternInvocation)) {
                for (EGroumActionNode targetInvocation : targetInvocations) {
                    if (targetInvocation.getLabel().equals(patternInvocation.getLabel())) {
                        addVertex(patternInvocation);
                        addEdge(patternNode, patternInvocation, pattern.getEdge(patternNode, patternInvocation));
                        extend(targetInvocation, patternInvocation);
                    }
                }
            }
        }
    }

}
