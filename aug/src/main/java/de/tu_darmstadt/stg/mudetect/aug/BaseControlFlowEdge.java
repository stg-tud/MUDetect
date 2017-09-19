package de.tu_darmstadt.stg.mudetect.aug;

public class BaseControlFlowEdge extends BaseEdge implements ControlFlowEdge {
    public BaseControlFlowEdge(Node source, Node target, Type type) {
        super(source, target, type);
    }
}
