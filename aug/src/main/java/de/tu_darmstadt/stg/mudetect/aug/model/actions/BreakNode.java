package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;

public class BreakNode extends BaseNode implements ActionNode {
    public BreakNode() {}

    public BreakNode(int sourceLineNumber) {
        super(sourceLineNumber);
    }

    @Override
    public String getLabel() {
        return "<break>";
    }

    @Override
    public boolean isCoreAction() {
        return true;
    }
}
