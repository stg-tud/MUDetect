package de.tu_darmstadt.stg.mudetect.aug.model;

public abstract class BaseEdge implements Edge {
    private final Node source;
    private final Node target;
    private final Type type;
    private APIUsageGraph graph;

    protected BaseEdge(Node source, Node target, Type type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    @Override
    public Node getSource() {
        return source;
    }

    @Override
    public Node getTarget() {
        return target;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return getSource() + "-(" + getLabel() + ")->" + getTarget();
    }
}
