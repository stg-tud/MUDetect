package de.tu_darmstadt.stg.mudetect.aug.model.actions;

public class UnaryOperatorNode extends OperatorNode {
    public UnaryOperatorNode(String operator) {
        super(operator);
    }

    public UnaryOperatorNode(String operator, int sourceLineNumber) {
        super(operator, sourceLineNumber);
    }
}
