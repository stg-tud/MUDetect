package de.tu_darmstadt.stg.mudetect.aug;

public class LiteralNode extends BaseNode implements DataNode {
    private final String dataType;
    private final String dataName;

    public LiteralNode(String dataType, String dataName) {
        this.dataType = dataType;
        this.dataName = dataName;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getValue() {
        return dataName;
    }

    @Override
    public String getType() {
        return dataType;
    }

    @Override
    public String getLabel() {
        return getType();
    }
}
