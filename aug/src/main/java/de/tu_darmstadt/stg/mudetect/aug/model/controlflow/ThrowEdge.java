package de.tu_darmstadt.stg.mudetect.aug.model.controlflow;

import de.tu_darmstadt.stg.mudetect.aug.model.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.ControlFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;

public class ThrowEdge extends BaseEdge implements ControlFlowEdge {
    public ThrowEdge(Node source, Node target) {
        super(source, target, Type.THROW);
    }
}
