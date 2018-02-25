package de.tu_darmstadt.stg.mudetect.aug.model.controlflow;

import de.tu_darmstadt.stg.mudetect.aug.model.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.ControlFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.DataFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.visitors.EdgeVisitor;

/**
 * Throw edges connect from the throwing action to the data node representing the exception, e.g.,
 * <code>inputStream.read() -(throw)-> IOException</code>. In a sense, they are comparable to
 * {@link de.tu_darmstadt.stg.mudetect.aug.model.dataflow.DefinitionEdge}s.
 */
public class ThrowEdge extends BaseEdge implements DataFlowEdge {
    public ThrowEdge(Node source, Node target) {
        super(source, target, Type.THROW);
    }

    @Override
    public <R> R apply(EdgeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
