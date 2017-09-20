package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class NullCheckNode extends InfixOperatorNode {
    public NullCheckNode() {
        super("<nullcheck>");
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
