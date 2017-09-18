package de.tu_darmstadt.stg.mudetect.matcher;

import de.tu_darmstadt.stg.mudetect.aug.Node;

import java.util.Optional;
import java.util.function.Function;

public interface NodeLabelProvider extends Function<Node, Optional<String>> {
    @SafeVarargs
    static Function<Node, String> firstOrDefaultLabel(Function<Node, Optional<String>>... gs) {
        return node -> {
            for (Function<Node, Optional<String>> getter : gs) {
                Optional<String> label = getter.apply(node);
                if (label.isPresent())
                    return label.get();
            }
            return node.getLabel();
        };
    }
}
