package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;

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

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
