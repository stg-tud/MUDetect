package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class ArrayAccessNode extends MethodCallNode {
    public ArrayAccessNode(String arrayTypeName) {
        super(arrayTypeName, "arrayget()");
    }

    public ArrayAccessNode(String arrayTypeName, int sourceLineNumber) {
        super(arrayTypeName, "arrayget()", sourceLineNumber);
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
