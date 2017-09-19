package de.tu_darmstadt.stg.mudetect.aug;

import java.util.Optional;

public class CastNode extends InvokationNode {
    private final String targetType;

    public CastNode(String targetType) {
        this.targetType = targetType;
    }

    @Override
    public String getLabel() {
        return targetType + ".<cast>";
    }

    @Override
    public Optional<String> getAPI() {
        return Optional.of(targetType);
    }
}
