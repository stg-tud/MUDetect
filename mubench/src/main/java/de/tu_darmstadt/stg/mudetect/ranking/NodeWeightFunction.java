package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.aug.model.Node;

import java.util.Collection;

public interface NodeWeightFunction {
    double getWeight(Node node);

    default double getWeight(Collection<Node> nodes) {
        return nodes.stream().mapToDouble(this::getWeight).sum();
    }

    default double getInverseWeight(Node node) {
        return 1 / getWeight(node);
    }

    default double getInverseWeight(Collection<Node> nodes) { return nodes.stream().mapToDouble(this::getInverseWeight).sum(); }
}
