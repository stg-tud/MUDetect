package de.tu_darmstadt.stg.eko.mudetect.model;

import egroum.EGroumActionNode;
import egroum.EGroumDataNode;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import java.util.HashSet;
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

    public Set<EGroumActionNode> getConditions(EGroumActionNode node) {
        Set<EGroumActionNode> conditions = new HashSet<>();
        for (EGroumEdge edge : edgesOf(node)) {
            if (getEdgeTarget(edge) == node && edge.isCond()) {
                conditions.add((EGroumActionNode) getEdgeSource(edge));
            }
        }
        return conditions;
    }
}
