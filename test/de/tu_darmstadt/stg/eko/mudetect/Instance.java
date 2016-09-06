package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import egroum.EGroumNode;
import org.jgrapht.graph.Subgraph;

import java.util.Set;

public class Instance extends Subgraph<EGroumNode, String, AUG> {
    public Instance(AUG base, Set<EGroumNode> vertexSubset, Set<String> edgeSubset) {
        super(base, vertexSubset, edgeSubset);
    }

    public Instance(AUG base, Set<EGroumNode> vertexSubset) {
        super(base, vertexSubset);
    }
}
