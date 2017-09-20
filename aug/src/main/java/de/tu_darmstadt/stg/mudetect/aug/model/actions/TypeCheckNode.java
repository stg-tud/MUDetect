package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class TypeCheckNode extends MethodCallNode {
    public TypeCheckNode(String targetTypeName) {
        super(targetTypeName, "<instanceof>");
    }

    public TypeCheckNode(String targetTypeName, int sourceLineNumber) {
        super(targetTypeName, "<instanceof>", sourceLineNumber);
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
