package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;

public class BreakNode extends BaseNode implements ActionNode {
    @Override
    public String getLabel() {
        return "<break>";
    }
}
