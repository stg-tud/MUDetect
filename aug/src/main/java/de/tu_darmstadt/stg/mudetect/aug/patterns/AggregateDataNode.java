package de.tu_darmstadt.stg.mudetect.aug.patterns;

import com.google.common.collect.Multiset;
import de.tu_darmstadt.stg.mudetect.aug.BaseNode;
import de.tu_darmstadt.stg.mudetect.aug.DataNode;

public class AggregateDataNode extends BaseNode implements DataNode {
    private Multiset<String> values;

    public AggregateDataNode(Multiset<String> values) {
        this.values = values;
    }

    public Multiset<String> getValues() {
        return values;
    }

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
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException();
    }
}
