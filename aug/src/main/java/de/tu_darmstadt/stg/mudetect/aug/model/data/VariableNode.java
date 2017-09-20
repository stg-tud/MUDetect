package de.tu_darmstadt.stg.mudetect.aug.model.data;

import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;
import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;

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
