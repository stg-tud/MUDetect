package de.tu_darmstadt.stg.mudetect.matcher;

import egroum.EGroumNode;

import java.util.Optional;
import java.util.function.Function;

public interface NodeLabelProvider extends Function<EGroumNode, Optional<String>> {
    @SafeVarargs
    static Function<EGroumNode, String> firstOrDefaultLabel(Function<EGroumNode, Optional<String>>... gs) {
        return node -> {
            for (Function<EGroumNode, Optional<String>> getter : gs) {
                Optional<String> label = getter.apply(node);
                if (label.isPresent())
                    return label.get();
            }
            return node.getLabel();
        };
    }
}
