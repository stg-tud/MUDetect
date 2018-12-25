package de.tu_darmstadt.stg.mudetect.aug.model;

import de.tu_darmstadt.stg.mudetect.aug.visitors.NodeVisitor;

import java.io.Serializable;
import java.util.Optional;

public interface Node extends Cloneable, Serializable {
    int getId();

    void setGraph(APIUsageGraph aug);

    /**
     * Nodes should not know the graph they belong to, otherwise we cannot safely reuse nodes. Once all usages of this
     * getter are migrated, we should also remove the setter.
     */
    @Deprecated
    APIUsageGraph getGraph();

    default boolean isCoreAction() {
        return false;
    }

    default Optional<String> getAPI() {
        return Optional.empty();
    }

    Node clone();

    <R> R apply(NodeVisitor<R> visitor);
}
