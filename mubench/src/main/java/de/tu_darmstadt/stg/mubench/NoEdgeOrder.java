package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mudetect.aug.model.Edge;

import java.util.function.BiPredicate;

public class NoEdgeOrder implements BiPredicate<Edge, Edge> {
    @Override
    public boolean test(Edge edge1, Edge edge2) {
        return false;
    }
}
