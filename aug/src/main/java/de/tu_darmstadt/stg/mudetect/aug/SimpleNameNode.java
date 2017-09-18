package de.tu_darmstadt.stg.mudetect.aug;

public class SimpleNameNode extends BaseNode implements DataNode {
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
        throw new UnsupportedOperationException();
    }
}
