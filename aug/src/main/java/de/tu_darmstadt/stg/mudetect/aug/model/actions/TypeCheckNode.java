package de.tu_darmstadt.stg.mudetect.aug.model.actions;

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
}
