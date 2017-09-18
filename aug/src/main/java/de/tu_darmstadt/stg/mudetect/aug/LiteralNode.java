package de.tu_darmstadt.stg.mudetect.aug;

public class LiteralNode extends BaseNode implements DataNode {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getValue() {
        return "<data>";
    }

    @Override
    public String getType() {
        return "<data>";
    }

    @Override
    public String getLabel() {
        return getType();
    }
}
