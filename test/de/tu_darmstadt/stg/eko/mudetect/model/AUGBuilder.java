package de.tu_darmstadt.stg.eko.mudetect.model;

import egroum.*;

public class AUGBuilder {
    private AUG aug = new AUG();

    public static AUGBuilder newAUG(EGroumNode... nodes) {
        AUGBuilder builder = new AUGBuilder();
        builder.withNodes(nodes);
        return builder;
    }

    private AUGBuilder withNode(EGroumNode node) {
        aug.addVertex(node);
        return this;
    }

    private AUGBuilder withNodes(EGroumNode... nodes) {
        for (EGroumNode node : nodes) {
            withNode(node);
        }
        return this;
    }

    public AUGBuilder withEdge(EGroumEdge edge) {
        aug.addEdge(edge.getSource(), edge.getTarget(), edge);
        return this;
    }

    public AUGBuilder withDataEdge(EGroumNode source, EGroumDataEdge.Type type, EGroumNode target) {
        return withEdge(new EGroumDataEdge(source, target, type));
    }

    public AUG build() {
        return aug;
    }
}
