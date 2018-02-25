package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.visitors.NodeVisitor;

public class TypeCheckNode extends OperatorNode {
    private final String targetTypeName;

    public TypeCheckNode(String targetTypeName) {
        super("<instanceof>");
        this.targetTypeName = targetTypeName;
    }

    public TypeCheckNode(String targetTypeName, int sourceLineNumber) {
        super("<instanceof>", sourceLineNumber);
        this.targetTypeName = targetTypeName;
    }

    @Override
    public String getLabel() {
        return targetTypeName + ".<instanceof>";
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
