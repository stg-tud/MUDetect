package de.tu_darmstadt.stg.mudetect.aug.model.controlflow;

import de.tu_darmstadt.stg.mudetect.aug.model.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.ControlFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.DataFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.visitors.EdgeVisitor;

/**
 * A handling edge represents data flow in the sense that the type information of the exception flows into the handling
 * code.
 */
public class ExceptionHandlingEdge extends BaseEdge implements DataFlowEdge {
    public ExceptionHandlingEdge(Node source, Node target) {
        super(source, target, Type.EXCEPTION_HANDLING);
    }

    @Override
    public <R> R apply(EdgeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
