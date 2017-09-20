package de.tu_darmstadt.stg.mudetect.aug.controlflow;

import de.tu_darmstadt.stg.mudetect.aug.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.ControlFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.Node;

public class OrderEdge extends BaseEdge implements ControlFlowEdge {
    public OrderEdge(Node source, Node target) {
        super(source, target, Type.ORDER);
    }
}
