package de.tu_darmstadt.stg.mudetect.model;

import egroum.EGroumActionNode;
import egroum.EGroumDataNode;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class AUG extends DirectedAcyclicGraph<EGroumNode, EGroumEdge> {

    public AUG() {
        super(EGroumEdge.class);
    }

    public EGroumDataNode getReceiver(EGroumNode node) {
        for (EGroumEdge edge : edgesOf(node)) {
            if (getEdgeTarget(edge) == node && edge.isRecv()) {
                return (EGroumDataNode) getEdgeSource(edge);
            }
        }
        return null;
    }

    public Set<EGroumActionNode> getInvocations(EGroumDataNode node) {
        Set<EGroumActionNode> invocations = new HashSet<>();
        for (EGroumEdge edge : edgesOf(node)) {
            if (getEdgeSource(edge) == node && edge.isRecv()) {
                invocations.add((EGroumActionNode) getEdgeTarget(edge));
            }
        }
        return invocations;
    }

    public Set<Condition> getConditions(EGroumActionNode node) {
        Set<Condition> conditions = new HashSet<>();
        for (EGroumEdge edge : edgesOf(node)) {
            if (getEdgeTarget(edge) == node && edge.isCond()) {
                EGroumActionNode edgeSource = (EGroumActionNode) getEdgeSource(edge);
                if (InfixExpression.Operator.toOperator(edgeSource.getLabel()) != null) {
                    // TODO clean the retrieval of operand arguments
                    Set<EGroumNode> operands = getArguments(edgeSource);
                    Iterator<EGroumNode> iterator = operands.iterator();
                    conditions.add(new Condition(iterator.next(), edgeSource, iterator.next()));
                } else {
                    conditions.add(new Condition(edgeSource));
                }
            }
        }
        return conditions;
    }

    public Set<EGroumNode> getArguments(EGroumActionNode node) {
        Set<EGroumNode> arguments = new HashSet<>();
        for (EGroumEdge edge : edgesOf(node)) {
            if (getEdgeTarget(edge) == node && edge.isParameter()) {
                arguments.add(edge.getSource());
            }
        }
        return arguments;
    }
}
