package de.tu_darmstadt.stg.mudetect.aug.actions;

import de.tu_darmstadt.stg.mudetect.aug.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.BaseNode;

import java.util.Optional;

public class CastNode extends BaseNode implements ActionNode {
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
