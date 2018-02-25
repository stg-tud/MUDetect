package de.tu_darmstadt.stg.mudetect.aug.model.controlflow;

import de.tu_darmstadt.stg.mudetect.aug.model.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.visitors.EdgeVisitor;

public class ContainsEdge extends BaseEdge {
    public ContainsEdge(Node source, Node target) {
        super(source, target, Type.CONTAINS);
    }

    @Override
    public <R> R apply(EdgeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
