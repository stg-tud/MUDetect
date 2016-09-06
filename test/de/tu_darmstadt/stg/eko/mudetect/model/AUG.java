package de.tu_darmstadt.stg.eko.mudetect.model;

import egroum.EGroumNode;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

public class AUG extends DirectedAcyclicGraph<EGroumNode, String> {
    public AUG() {
        super(String.class);
    }
}
