package de.tu_darmstadt.stg.mudetect.aug.dataflow;

import de.tu_darmstadt.stg.mudetect.aug.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.DataFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.Node;

public class BaseDataFlowEdge extends BaseEdge implements DataFlowEdge {
    public BaseDataFlowEdge(Node source, Node target, Type type) {
        super(source, target, type);
    }
}
