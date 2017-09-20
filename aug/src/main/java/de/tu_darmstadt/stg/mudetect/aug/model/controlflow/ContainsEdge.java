package de.tu_darmstadt.stg.mudetect.aug.model.controlflow;

import de.tu_darmstadt.stg.mudetect.aug.model.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.ControlFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;

public class ContainsEdge extends BaseEdge implements ControlFlowEdge {
    public ContainsEdge(Node source, Node target) {
        super(source, target, Type.CONTAINS);
    }
}
