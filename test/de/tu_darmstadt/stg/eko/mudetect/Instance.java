package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.graph.Subgraph;

import java.util.Set;

public class Instance extends Subgraph<EGroumNode, EGroumEdge, AUG> {
    public Instance(AUG base, Set<EGroumNode> vertexSubset, Set<EGroumEdge> edgeSubset) {
        super(base, vertexSubset, edgeSubset);
    }

    public Instance(AUG base, Set<EGroumNode> vertexSubset) {
        super(base, vertexSubset);
    }
}
