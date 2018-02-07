package de.tu_darmstadt.stg.mudetect.aug.model.patterns;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;
import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AggregateDataNode extends BaseNode implements DataNode {
    private final String dataType;
    private Set<DataNode> aggregatedNodes;

    public AggregateDataNode(String dataType, Set<DataNode> aggregatedNodes) {
        this.dataType = dataType;
        this.aggregatedNodes = aggregatedNodes;
    }

    @Override
    public String getLabel() {
        return getType();
    }

    @Override
    public String getName() {
        return getFirstOrNull(getAggregatedNames());
    }

    public Multiset<String> getAggregatedNames() {
        return mapAggregatedNodes(DataNode::getName);
    }

    @Override
    public String getValue() {
        return getFirstOrNull(getAggregatedValues());
    }

    public Multiset<String> getAggregatedValues() {
        return mapAggregatedNodes(DataNode::getValue);
    }

    @Override
    public String getType() {
        return dataType;
    }

    private HashMultiset<String> mapAggregatedNodes(Function<DataNode, String> getName) {
        return HashMultiset.create(aggregatedNodes.stream()
                .map(getName).filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    private String getFirstOrNull(Multiset<String> aggregatedNames) {
        if (aggregatedNames.isEmpty()) {
            return null;
        } else {
            return aggregatedNames.iterator().next();
        }
    }
}
