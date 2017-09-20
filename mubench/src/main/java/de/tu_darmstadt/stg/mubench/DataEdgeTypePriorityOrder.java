package de.tu_darmstadt.stg.mubench;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;

import java.util.function.BiPredicate;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.*;


public class DataEdgeTypePriorityOrder implements BiPredicate<Edge, Edge> {
    private static final Multimap<Edge.Type, Edge.Type> EDGE_TYPE_RELATION =
            HashMultimap.create();

    static {
        EDGE_TYPE_RELATION.put(RECEIVER, CONDITION);
        EDGE_TYPE_RELATION.put(RECEIVER, PARAMETER);
        EDGE_TYPE_RELATION.put(RECEIVER, ORDER);
        EDGE_TYPE_RELATION.put(DEFINITION, CONDITION);
        EDGE_TYPE_RELATION.put(DEFINITION, PARAMETER);
        EDGE_TYPE_RELATION.put(DEFINITION, ORDER);
        EDGE_TYPE_RELATION.put(CONDITION, PARAMETER);
        // THROW
        // FINALLY
        // QUALIFIER
        // ORDER
        // CONTAINS
    }

    @Override
    public boolean test(Edge edge1, Edge edge2) {
        Edge.Type edge1Type = edge1.getType();
        Edge.Type edge2Type = edge2.getType();
        return EDGE_TYPE_RELATION.containsKey(edge1Type) && EDGE_TYPE_RELATION.containsEntry(edge1Type, edge2Type);
    }
}
