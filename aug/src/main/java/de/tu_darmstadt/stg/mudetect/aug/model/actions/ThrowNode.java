package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;

public class ThrowNode extends BaseNode implements ActionNode {
    public ThrowNode() {}

    public ThrowNode(int sourceLineNumber) {
        super(sourceLineNumber);
    }

    @Override
    public String getLabel() {
        return "<throw>";
    }

    @Override
    public boolean isCoreAction() {
        return true;
    }
}
