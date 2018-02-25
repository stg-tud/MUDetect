package de.tu_darmstadt.stg.mudetect.aug.model.dataflow;

import de.tu_darmstadt.stg.mudetect.aug.model.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.DataFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;

public abstract class BaseDataFlowEdge extends BaseEdge implements DataFlowEdge {
    public BaseDataFlowEdge(Node source, Node target, Type type) {
        super(source, target, type);
    }
}
