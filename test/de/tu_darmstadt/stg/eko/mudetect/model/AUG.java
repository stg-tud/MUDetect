package de.tu_darmstadt.stg.eko.mudetect.model;

import egroum.EGroumDataNode;
import egroum.EGroumNode;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

public class AUG extends DirectedAcyclicGraph<EGroumNode, String> {

    public AUG() {
        super(String.class);
    }

    public EGroumDataNode getReceiver(EGroumNode actionNode) {
        for (String edge : edgesOf(actionNode)) {
            if (getEdgeTarget(edge) == actionNode) {
                return (EGroumDataNode) getEdgeSource(edge);
            }
        }
        return null;
    }
}
