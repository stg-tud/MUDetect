package de.tu_darmstadt.stg.mudetect.aug;

public class VariableNode extends BaseNode implements DataNode {
    private final String type;

    public VariableNode(String type) {
        this.type = type;
    }

    @Override
    public String getLabel() {
        return "<data>";
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public String getType() {
        return type;
    }
}
