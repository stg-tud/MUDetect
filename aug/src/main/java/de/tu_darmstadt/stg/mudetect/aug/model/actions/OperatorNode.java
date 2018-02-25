package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;
import de.tu_darmstadt.stg.mudetect.aug.visitors.NodeVisitor;

public abstract class OperatorNode extends BaseNode implements ActionNode {
    private final String operator;

    OperatorNode(String operator) {
        this.operator = operator;
    }

    OperatorNode(String operator, int sourceLineNumber) {
        super(sourceLineNumber);
        this.operator = operator;
    }

    @Override
    public String getLabel() {
        return operator;
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }

    @Override
    public <R> R apply(NodeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
