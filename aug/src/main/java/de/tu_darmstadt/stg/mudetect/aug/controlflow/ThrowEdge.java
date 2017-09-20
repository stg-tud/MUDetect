package de.tu_darmstadt.stg.mudetect.aug.controlflow;

import de.tu_darmstadt.stg.mudetect.aug.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.ControlFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.Node;

public class ThrowEdge extends BaseEdge implements ControlFlowEdge {
    public ThrowEdge(Node source, Node target) {
        super(source, target, Type.THROW);
    }
}
