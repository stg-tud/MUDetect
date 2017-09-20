package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class SuperConstructorCallNode extends ConstructorCallNode {
    public SuperConstructorCallNode(String methodSignature) {
        super(methodSignature);
    }

    public SuperConstructorCallNode(String methodSignature, int sourceLineNumber) {
        super(methodSignature, sourceLineNumber);
    }
}
