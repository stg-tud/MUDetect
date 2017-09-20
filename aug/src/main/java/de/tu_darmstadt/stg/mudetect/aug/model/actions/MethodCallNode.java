package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;

import java.util.Optional;

public class MethodCallNode extends BaseNode implements ActionNode {
    private final String declaringTypeAndMethodSignature;

    public MethodCallNode(String declaringTypeAndMethodSignature) {
        this.declaringTypeAndMethodSignature = declaringTypeAndMethodSignature;
    }

    @Override
    public boolean isCoreAction() {
        return !getMethodSignature().startsWith("get");
    }

    @Override
    public String getLabel() {
        return declaringTypeAndMethodSignature;
    }

    @Override
    public Optional<String> getAPI() {
        String declaringType = getDeclaringType();
        if (!declaringType.isEmpty())
            return Optional.of(declaringType);
        else
            return Optional.empty();
    }

    private String getDeclaringType() {
        return getLabel().substring(0, getLabel().length() - getMethodSignature().length());
    }

    private String getMethodSignature() {
        int endOfDeclaringTypeName = getLabel().indexOf('.');
        return getLabel().substring(endOfDeclaringTypeName + 1);
    }
}
