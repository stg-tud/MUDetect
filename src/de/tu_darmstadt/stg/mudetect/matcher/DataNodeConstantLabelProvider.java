package de.tu_darmstadt.stg.mudetect.matcher;

import egroum.EGroumDataNode;
import egroum.EGroumNode;

import java.util.Optional;

public class DataNodeConstantLabelProvider implements NodeLabelProvider {
    @Override
    public Optional<String> apply(EGroumNode node) {
        return node instanceof EGroumDataNode ? Optional.of("Object") : Optional.empty();
    }
}
