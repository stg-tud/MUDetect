package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class NullCheckNode extends InfixOperatorNode {
    public NullCheckNode() {
        super("<nullcheck>");
    }

    public NullCheckNode(int sourceLineNumber) {
        super("<nullcheck>", sourceLineNumber);
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
