package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.visitors.NodeVisitor;

public class UnaryOperatorNode extends OperatorNode {
    public UnaryOperatorNode(String operator) {
        super(operator);
    }

    public UnaryOperatorNode(String operator, int sourceLineNumber) {
        super(operator, sourceLineNumber);
    }

    @Override
    public <R> R apply(NodeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
