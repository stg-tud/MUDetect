package de.tu_darmstadt.stg.mubench;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import egroum.EGroumDataEdge;
import egroum.EGroumEdge;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import static egroum.EGroumDataEdge.Type.*;

public class DataEdgeTypePriorityOrder implements BiPredicate<EGroumEdge, EGroumEdge> {
    private static final Multimap<EGroumDataEdge.Type, EGroumDataEdge.Type> EDGE_TYPE_RELATION =
            HashMultimap.create();

    static {
        EDGE_TYPE_RELATION.put(RECEIVER, CONDITION);
        EDGE_TYPE_RELATION.put(RECEIVER, PARAMETER);
        EDGE_TYPE_RELATION.put(DEFINITION, CONDITION);
        EDGE_TYPE_RELATION.put(DEFINITION, PARAMETER);
        EDGE_TYPE_RELATION.put(CONDITION, PARAMETER);
        // THROW
        // FINALLY
        // QUALIFIER
        // ORDER
        // CONTAINS
    }

    @Override
    public boolean test(EGroumEdge edge1, EGroumEdge edge2) {
        if (edge1 instanceof EGroumDataEdge && edge2 instanceof EGroumDataEdge) {
            EGroumDataEdge.Type edge1Type = ((EGroumDataEdge) edge1).getType();
            EGroumDataEdge.Type edge2Type = ((EGroumDataEdge) edge2).getType();
            return EDGE_TYPE_RELATION.containsKey(edge1Type) && EDGE_TYPE_RELATION.containsEntry(edge1Type, edge2Type);
        }
        return false;
    }
}
