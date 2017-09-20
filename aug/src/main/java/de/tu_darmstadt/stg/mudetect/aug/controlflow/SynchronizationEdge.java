package de.tu_darmstadt.stg.mudetect.aug.controlflow;

import de.tu_darmstadt.stg.mudetect.aug.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.ControlFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.Node;

public class SynchronizationEdge extends BaseEdge implements ControlFlowEdge {
    public SynchronizationEdge(Node source, Node target) {
        super(source, target, Type.SYNCHRONIZE);
    }
}
