package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class ArrayAssignmentNode extends MethodCallNode {
    public ArrayAssignmentNode(String declaringTypeAndMethodSignature) {
        super(declaringTypeAndMethodSignature);
    }

    public ArrayAssignmentNode(String declaringTypeAndMethodSignature, int sourceLineNumber) {
        super(declaringTypeAndMethodSignature, sourceLineNumber);
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
