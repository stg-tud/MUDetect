package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;

import java.util.Optional;

public class MethodCallNode extends BaseNode implements ActionNode {
    private final String declaringTypeName;
    private final String methodSignature;
    private String labelCache = null;

    public MethodCallNode(String declaringTypeName, String methodSignature) {
        this.declaringTypeName = declaringTypeName;
        this.methodSignature = methodSignature;
    }

    public MethodCallNode(String declaringTypeName, String methodSignature, int sourceLineNumber) {
        super(sourceLineNumber);
        this.declaringTypeName = declaringTypeName;
        this.methodSignature = methodSignature;
    }

    @Override
    public boolean isCoreAction() {
        return !getMethodSignature().startsWith("get");
    }

    @Override
    public String getLabel() {
        if (labelCache == null) {
            labelCache = getDeclaringTypeName() + "." + getMethodSignature();
        }
        return labelCache;
    }

    @Override
    public Optional<String> getAPI() {
        String declaringType = getDeclaringTypeName();
        if (!declaringType.isEmpty() && !declaringType.endsWith("[]"))
            return Optional.of(declaringType);
        else
            return Optional.empty();
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public String getDeclaringTypeName() {
        return declaringTypeName;
    }
}
