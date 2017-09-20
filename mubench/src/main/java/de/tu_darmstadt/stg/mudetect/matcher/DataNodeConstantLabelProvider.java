package de.tu_darmstadt.stg.mudetect.matcher;

import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;

import java.util.Optional;

public class DataNodeConstantLabelProvider implements NodeLabelProvider {
    @Override
    public Optional<String> apply(Node node) {
        return node instanceof DataNode ? Optional.of("Object") : Optional.empty();
    }
}
