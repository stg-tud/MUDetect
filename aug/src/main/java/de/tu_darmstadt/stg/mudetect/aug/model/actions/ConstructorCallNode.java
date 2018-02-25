package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.visitors.NodeVisitor;

public class ConstructorCallNode extends MethodCallNode {
    public ConstructorCallNode(String typeName) {
        super(typeName, "<init>");
    }

    public ConstructorCallNode(String typeName, int sourceLineNumber) {
        super(typeName, "<init>", sourceLineNumber);
    }

    @Override
    public boolean isCoreAction() {
        return true;
    }

    @Override
    public <R> R apply(NodeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
