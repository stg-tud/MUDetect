package de.tu_darmstadt.stg.mudetect.aug;

public class AnonymousClassMethod extends BaseNode implements DataNode {
    private final String methodSignature;

    public AnonymousClassMethod(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    @Override
    public String getType() {
        return "<method>";
    }

    @Override
    public String getName() {
        return "<method>";
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public String getLabel() {
        return methodSignature;
    }
}
