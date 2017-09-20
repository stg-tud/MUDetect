package de.tu_darmstadt.stg.mudetect.aug.controlflow;

import de.tu_darmstadt.stg.mudetect.aug.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.ControlFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.Node;

public class FinallyEdge extends BaseEdge implements ControlFlowEdge {
    public FinallyEdge(Node source, Node target) {
        super(source, target, Type.FINALLY);
    }
}
