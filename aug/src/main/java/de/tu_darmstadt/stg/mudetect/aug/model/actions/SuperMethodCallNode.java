package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class SuperMethodCallNode extends MethodCallNode {
    public SuperMethodCallNode(String declaringTypeName, String methodSignature) {
        super(declaringTypeName, methodSignature);
    }

    public SuperMethodCallNode(String declaringTypeName, String methodSignature, int sourceLineNumber) {
        super(declaringTypeName, methodSignature, sourceLineNumber);
    }
}
