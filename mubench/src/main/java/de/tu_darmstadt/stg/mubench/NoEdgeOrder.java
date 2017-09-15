package de.tu_darmstadt.stg.mubench;

import egroum.EGroumEdge;

import java.util.function.BiPredicate;

public class NoEdgeOrder implements BiPredicate<EGroumEdge, EGroumEdge> {
    @Override
    public boolean test(EGroumEdge edge1, EGroumEdge edge2) {
        return false;
    }
}
