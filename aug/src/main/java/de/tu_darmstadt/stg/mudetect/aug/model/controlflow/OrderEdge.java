package de.tu_darmstadt.stg.mudetect.aug.model.controlflow;

import de.tu_darmstadt.stg.mudetect.aug.model.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.ControlFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;

public class OrderEdge extends BaseEdge implements ControlFlowEdge {
    public OrderEdge(Node source, Node target) {
        super(source, target, Type.ORDER);
    }
}
