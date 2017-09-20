package de.tu_darmstadt.stg.mudetect.aug.model.patterns;

import com.google.common.collect.Multiset;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;
import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;

public class AggregateDataNode extends BaseNode implements DataNode {
    private final String dataType;
    private Multiset<String> dataNames;

    public AggregateDataNode(String dataType, Multiset<String> dataNames) {
        this.dataType = dataType;
        this.dataNames = dataNames;
    }

    public Multiset<String> getDataNames() {
        return dataNames;
    }

    @Override
    public String getLabel() {
        return dataType;
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
