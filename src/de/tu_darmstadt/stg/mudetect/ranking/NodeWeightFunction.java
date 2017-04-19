package de.tu_darmstadt.stg.mudetect.ranking;

import egroum.EGroumNode;

import java.util.Collection;

public interface NodeWeightFunction {
    double getWeight(EGroumNode node);

    default double getWeight(Collection<EGroumNode> nodes) {
        return nodes.stream().mapToDouble(this::getWeight).sum();
    }

    default double getInverseWeight(EGroumNode node) {
        return 1 / getWeight(node);
    }

    default double getInverseWeight(Collection<EGroumNode> nodes) { return nodes.stream().mapToDouble(this::getInverseWeight).sum(); }
}
