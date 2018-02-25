package de.tu_darmstadt.stg.mudetect.aug.model.data;

import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;
import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.visitors.NodeVisitor;

public class ConstantNode extends BaseNode implements DataNode {
    private final String dataType;
    private final String dataName;
    private final String dataValue;

    public ConstantNode(String dataType, String dataName, String dataValue) {
        this.dataType = dataType;
        this.dataName = dataName;
        this.dataValue = dataValue;
    }

    @Override
    public String getName() {
        return dataName;
    }

    @Override
    public String getValue() {
        return dataValue;
    }

    @Override
    public String getType() {
        return dataType;
    }

    @Override
    public <R> R apply(NodeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
