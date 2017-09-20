package de.tu_darmstadt.stg.mudetect.aug.data;

import de.tu_darmstadt.stg.mudetect.aug.BaseNode;
import de.tu_darmstadt.stg.mudetect.aug.DataNode;

public class VariableNode extends BaseNode implements DataNode {
    private final String dataType;
    private final String dataName;

    public VariableNode(String dataType, String dataName) {
        this.dataType = dataType;
        this.dataName = dataName;
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
    public String getType() {
        return dataType;
    }

    @Override
    public String getLabel() {
        return getType();
    }
}
