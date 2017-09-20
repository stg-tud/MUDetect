package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class TypeCheckNode extends MethodCallNode {
    public TypeCheckNode(String targetTypeName) {
        super(targetTypeName, "<instanceof>");
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
