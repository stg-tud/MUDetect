package de.tu_darmstadt.stg.mudetect.aug.actions;

public class ConstructorCallNode extends MethodCallNode {
    public ConstructorCallNode(String methodSignature) {
        super(methodSignature);
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
