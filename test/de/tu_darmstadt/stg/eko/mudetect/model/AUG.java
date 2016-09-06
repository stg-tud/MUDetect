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

    public EGroumDataNode getReceiver(EGroumNode actionNode) {
        for (EGroumEdge edge : edgesOf(actionNode)) {
            if (getEdgeTarget(edge) == actionNode && edge.isRecv()) {
                return (EGroumDataNode) getEdgeSource(edge);
            }
        }
        return null;
    }

    public Set<EGroumActionNode> getInvocations(EGroumDataNode dataNode) {
        Set<EGroumActionNode> invocations = new HashSet<>();
        for (EGroumEdge edge : edgesOf(dataNode)) {
            if (getEdgeSource(edge) == dataNode && edge.isRecv()) {
                invocations.add((EGroumActionNode) getEdgeTarget(edge));
            }
        }
        return invocations;
    }
}
