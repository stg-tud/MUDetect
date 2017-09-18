package de.tu_darmstadt.stg.mudetect.aug;

import java.util.Optional;

public class MethodCallNode extends InvokationNode implements ActionNode {
    private final String methodSignature;

    public MethodCallNode(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    @Override
    public boolean isCoreAction() {
        return !getLabel().startsWith("get");
    }

    @Override
    public String getLabel() {
        return methodSignature;
    }

    @Override
    public Optional<String> getAPI() {
        return Optional.of(getLabel().split("\\.")[0]);
    }
}
