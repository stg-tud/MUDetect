package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class ConstructorCallNode extends MethodCallNode {
    public ConstructorCallNode(String typeName) {
        super(typeName, "<init>");
    }

    public ConstructorCallNode(String typeName, int sourceLineNumber) {
        super(typeName, "<init>", sourceLineNumber);
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
