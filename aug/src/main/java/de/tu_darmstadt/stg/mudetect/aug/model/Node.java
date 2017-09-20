package de.tu_darmstadt.stg.mudetect.aug.model;

import java.util.Optional;

public interface Node extends Cloneable {
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

    String getLabel();

    default Optional<String> getAPI() {
        return Optional.empty();
    }

    Node clone();
}