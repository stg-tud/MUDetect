package de.tu_darmstadt.stg.eko.mudetect.model;

import egroum.EGroumDataNode;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

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
}
