package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;

public class ContinueNode extends BaseNode implements ActionNode {
    public ContinueNode() {}

    public ContinueNode(int sourceLineNumber) {
        super(sourceLineNumber);
    }

    @Override
    public String getLabel() {
        return "<continue>";
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
