package de.tu_darmstadt.stg.mudetect.aug.model;

import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;

public abstract class BaseEdge implements Edge {
    private Node source;
    private Node target;
    private final Type type;
    private APIUsageGraph graph;

    protected BaseEdge(Node source, Node target, Type type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    /**
     * Try fetching this information from the corresponding graph, because this would allow us to get rid of these
     * fields and to safely reuse edges between multiple graphs, instead of cloning them.
     */
    @Deprecated
    @Override
    public Node getSource() {
        return source;
    }

    /**
     * Try fetching this information from the corresponding graph, because this would allow us to get rid of these
     * fields and to safely reuse edges between multiple graphs, instead of cloning them.
     */
    @Deprecated
    @Override
    public Node getTarget() {
        return target;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Edge clone() {
        try {
            return (Edge) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("All edges must be cloneable.", e);
        }
    }

    @Override
    public Edge clone(Node newSourceNode, Node newTargetNode) {
        BaseEdge clone = (BaseEdge) clone();
        clone.source = newSourceNode;
        clone.target = newTargetNode;
        return clone;
    }

    @Override
    public String toString() {
        return getSource() + "-(" + new BaseAUGLabelProvider().getLabel(this) + ")->" + getTarget();
    }
}
