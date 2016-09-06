package de.tu_darmstadt.stg.eko.mudetect.model;

import egroum.EGroumNode;
import org.jgrapht.graph.builder.DirectedGraphBuilder;

public class AUGTestUtils {
    public static DirectedGraphBuilder<EGroumNode, String, AUG> createAUG(EGroumNode... nodes) {
        return new DirectedGraphBuilder<>(new AUG()).addVertices(nodes);
    }
}
