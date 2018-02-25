package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;
import de.tu_darmstadt.stg.mudetect.aug.visitors.NodeVisitor;

import java.util.Optional;

public class CastNode extends BaseNode implements ActionNode {
    private final String targetType;

    public CastNode(String targetType) {
        this.targetType = targetType;
    }

    public CastNode(String targetType, int sourceLineNumber) {
        super(sourceLineNumber);
        this.targetType = targetType;
    }

    public String getTargetType() {
        return targetType;
    }

    @Override
    public Optional<String> getAPI() {
        return Optional.of(targetType);
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }

    @Override
    public <R> R apply(NodeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
