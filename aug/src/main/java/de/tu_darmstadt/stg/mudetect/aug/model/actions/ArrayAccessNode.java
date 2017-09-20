package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class ArrayAccessNode extends MethodCallNode {
    public ArrayAccessNode(String declaringTypeAndMethodSignature) {
        super(declaringTypeAndMethodSignature);
    }

    public ArrayAccessNode(String declaringTypeAndMethodSignature, int sourceLineNumber) {
        super(declaringTypeAndMethodSignature, sourceLineNumber);
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
