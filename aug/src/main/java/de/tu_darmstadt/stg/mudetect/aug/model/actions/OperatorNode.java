package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;

abstract class OperatorNode extends BaseNode implements ActionNode {
    private final String operator;

    public OperatorNode(String operator) {
        this.operator = operator;
    }

    @Override
    public String getLabel() {
        return operator;
    }
}
