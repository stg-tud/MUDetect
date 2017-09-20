package de.tu_darmstadt.stg.mudetect.aug.actions;

import de.tu_darmstadt.stg.mudetect.aug.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.BaseNode;

public class AssignmentNode extends BaseNode implements ActionNode {
    @Override
    public String getLabel() {
        return "=";
    }
}
