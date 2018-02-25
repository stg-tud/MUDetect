package de.tu_darmstadt.stg.mudetect.aug.visitors;

import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;

public interface AUGLabelProvider extends AUGElementVisitor<String> {
    default String getLabel(Node node) {
        return node.apply(this);
    }

    default String getLabel(Edge edge) {
        return edge.apply(this);
    }
}
