package de.tu_darmstadt.stg.mudetect.aug;

public class ExceptionDataNode extends BaseNode implements DataNode {
    private final String dataType;
    private final String dataName;

    public ExceptionDataNode(String dataType, String dataName) {
        this.dataType = dataType;
        this.dataName = dataName;
    }

    @Override
    public String getType() {
        return dataType;
    }

    @Override
    public String getName() {
        return dataName;
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
