package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class SuperMethodCallNode extends MethodCallNode {
    public SuperMethodCallNode(String methodSignature) {
        super(methodSignature);
    }

    public SuperMethodCallNode(String methodSignature, int sourceLineNumber) {
        super(methodSignature, sourceLineNumber);
    }
}
