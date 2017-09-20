package de.tu_darmstadt.stg.mudetect.aug.model;

import java.util.Optional;

public interface Node {
    int getId();

    void setGraph(APIUsageGraph aug);

    APIUsageGraph getGraph();

    default boolean isCoreAction() {
        return false;
    }

    String getLabel();

    default Optional<String> getAPI() {
        return Optional.empty();
    }
}
