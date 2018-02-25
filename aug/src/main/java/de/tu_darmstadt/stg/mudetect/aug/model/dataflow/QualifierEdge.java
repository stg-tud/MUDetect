package de.tu_darmstadt.stg.mudetect.aug.model.dataflow;

import de.tu_darmstadt.stg.mudetect.aug.model.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.DataFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.visitors.EdgeVisitor;

public class QualifierEdge extends BaseEdge implements DataFlowEdge {
    public QualifierEdge(Node source, Node target) {
        super(source, target, Type.QUALIFIER);
    }

    @Override
    public <R> R apply(EdgeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
