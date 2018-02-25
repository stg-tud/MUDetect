package de.tu_darmstadt.stg.mudetect.aug.visitors;

import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.*;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.DefinitionEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ParameterEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.QualifierEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ReceiverEdge;

public interface EdgeVisitor<R> {
    // Control Flow
    R visit(ContainsEdge edge);
    R visit(ExceptionHandlingEdge edge);
    R visit(FinallyEdge edge);
    R visit(OrderEdge edge);
    R visit(RepetitionEdge edge);
    R visit(SelectionEdge edge);
    R visit(SynchronizationEdge edge);
    R visit(ThrowEdge edge);
    // Data Flow
    R visit(DefinitionEdge edge);
    R visit(ParameterEdge edge);
    R visit(QualifierEdge edge);
    R visit(ReceiverEdge edge);
}
