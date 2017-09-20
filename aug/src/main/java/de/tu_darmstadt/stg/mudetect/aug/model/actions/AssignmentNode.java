package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;

public class AssignmentNode extends BaseNode implements ActionNode {
    public AssignmentNode() {}

    public AssignmentNode(int sourceLineNumber) {
        super(sourceLineNumber);
    }

    @Override
    public String getLabel() {
        return "=";
    }

    @Override
    public boolean isCoreAction() {
        return true;
    }
}
