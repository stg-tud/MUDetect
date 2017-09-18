package de.tu_darmstadt.stg.mudetect.aug;

import java.util.Optional;

public interface Node {
    int getId();

    void setGraph(APIUsageGraph aug);

    APIUsageGraph getGraph();

    boolean isCoreAction();

    String getLabel();

    default Optional<String> getAPI() {
        return Optional.empty();
    }
}
