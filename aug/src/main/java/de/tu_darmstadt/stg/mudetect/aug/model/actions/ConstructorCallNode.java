package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class ConstructorCallNode extends MethodCallNode {
    public ConstructorCallNode(String methodSignature) {
        super(methodSignature);
    }

    @Override
    public boolean isCoreAction() {
        return true;
    }
}
