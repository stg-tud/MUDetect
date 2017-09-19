package de.tu_darmstadt.stg.mudetect.aug;

import java.util.Optional;

public class UnaryOperatorNode extends InvokationNode {
    private final String operator;

    public UnaryOperatorNode(String operator) {
        this.operator = operator;
    }

    @Override
    public String getLabel() {
        return operator;
    }

    @Override
    public Optional<String> getAPI() {
        return Optional.empty();
    }
}
