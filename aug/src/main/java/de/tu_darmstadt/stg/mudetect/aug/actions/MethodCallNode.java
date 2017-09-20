package de.tu_darmstadt.stg.mudetect.aug.actions;

import de.tu_darmstadt.stg.mudetect.aug.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.BaseNode;

import java.util.Optional;

public class MethodCallNode extends BaseNode implements ActionNode {
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
        if (getLabel().contains("."))
            return Optional.of(getLabel().split("\\.")[0]);
        else
            return Optional.empty();
    }
}
