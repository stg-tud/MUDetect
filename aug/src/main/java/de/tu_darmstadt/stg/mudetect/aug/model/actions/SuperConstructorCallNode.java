package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class SuperConstructorCallNode extends ConstructorCallNode {
    public SuperConstructorCallNode(String superTypeName) {
        super(superTypeName);
    }

    public SuperConstructorCallNode(String superTypeName, int sourceLineNumber) {
        super(superTypeName, sourceLineNumber);
    }

    @Override
    public String getLabel() {
        return getDeclaringTypeName() + "()";
    }
}
