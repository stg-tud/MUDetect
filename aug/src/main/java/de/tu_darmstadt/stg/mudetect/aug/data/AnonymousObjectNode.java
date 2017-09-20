package de.tu_darmstadt.stg.mudetect.aug.data;

import de.tu_darmstadt.stg.mudetect.aug.BaseNode;
import de.tu_darmstadt.stg.mudetect.aug.DataNode;

public class AnonymousObjectNode extends BaseNode implements DataNode {
    private final String dataType;

    public AnonymousObjectNode(String dataType) {
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
