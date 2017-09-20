package de.tu_darmstadt.stg.mudetect.aug.model;

import java.util.Optional;

public class APIUsageExample extends APIUsageGraph {
    private final Location location;

    public APIUsageExample(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public Optional<Integer> getSourceLineNumber(Node node) {
        return node instanceof BaseNode ? ((BaseNode) node).getSourceLineNumber() : Optional.empty();
    }
}
