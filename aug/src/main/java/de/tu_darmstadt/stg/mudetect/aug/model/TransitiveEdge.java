package de.tu_darmstadt.stg.mudetect.aug.model;

import de.tu_darmstadt.stg.mudetect.aug.visitors.EdgeVisitor;

public class TransitiveEdge extends BaseEdge implements Edge {
    private Edge correspondingDirectEdge;

    public TransitiveEdge(Node source, Node target, Edge correspondingDirectEdge) {
        super(source, target, correspondingDirectEdge.getType());
        this.correspondingDirectEdge = correspondingDirectEdge;
    }

    public Edge getCorrespondingDirectEdge() {
        return correspondingDirectEdge;
    }

    @Override
    public boolean isDirect() {
        return false;
    }

    @Override
    public <R> R apply(EdgeVisitor<R> visitor) {
        return getCorrespondingDirectEdge().apply(visitor);
    }
}
