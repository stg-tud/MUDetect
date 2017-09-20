package de.tu_darmstadt.stg.mudetect.aug.dataflow;

import de.tu_darmstadt.stg.mudetect.aug.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.DataFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.Node;

public class ReceiverEdge extends BaseEdge implements DataFlowEdge {
    public ReceiverEdge(Node source, Node target) {
        super(source, target, Type.RECEIVER);
    }
}
