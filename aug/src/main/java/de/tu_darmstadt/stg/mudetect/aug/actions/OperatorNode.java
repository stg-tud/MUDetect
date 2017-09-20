package de.tu_darmstadt.stg.mudetect.aug.actions;

import de.tu_darmstadt.stg.mudetect.aug.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.BaseNode;

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
