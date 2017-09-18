package de.tu_darmstadt.stg.mudetect.aug;

public class InfixOperatorNode extends InvokationNode {
    private final String operator;

    public InfixOperatorNode(String operator) {
        this.operator = operator;
    }

    @Override
    public String getLabel() {
        return operator;
    }
}
