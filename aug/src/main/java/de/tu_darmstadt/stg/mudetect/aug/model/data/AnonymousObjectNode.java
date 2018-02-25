package de.tu_darmstadt.stg.mudetect.aug.model.data;

import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;
import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.visitors.NodeVisitor;

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
    public <R> R apply(NodeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
