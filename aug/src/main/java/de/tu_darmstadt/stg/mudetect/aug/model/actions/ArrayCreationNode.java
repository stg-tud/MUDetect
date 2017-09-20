package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class ArrayCreationNode extends ConstructorCallNode {
    public ArrayCreationNode(String baseType) {
        super(baseType);
    }

    public ArrayCreationNode(String baseType, int sourceLineNumber) {
        super(baseType, sourceLineNumber);
    }

    @Override
    public String getLabel() {
        return "{" + getDeclaringTypeName() + "}";
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
