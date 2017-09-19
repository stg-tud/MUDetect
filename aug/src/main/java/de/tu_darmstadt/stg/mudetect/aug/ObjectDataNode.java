package de.tu_darmstadt.stg.mudetect.aug;

public class ObjectDataNode extends BaseNode implements DataNode {
    private final String dataType;

    public ObjectDataNode(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public String getType() {
        return dataType;
    }

    @Override
    public String getName() {
        return "Some(" + dataType + ")";
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public String getLabel() {
        return getType();
    }
}
