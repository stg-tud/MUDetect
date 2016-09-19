package de.tu_darmstadt.stg.mudetect.model;

import egroum.*;
import egroum.EGroumDataEdge.Type;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static egroum.EGroumDataEdge.Type.*;

public class AUG extends DirectedAcyclicGraph<EGroumNode, EGroumEdge> {

    private final Location location;

    public AUG(String name, String filePath) {
        super(EGroumEdge.class);
        this.location = new Location(filePath, name);
    }

    public Location getLocation() {
        return location;
    }

    public Map<String, Set<EGroumEdge>> getInEdgesByType(EGroumNode node) {
        return getEdgesByType(node, edge -> getEdgeTarget(edge) == node);
    }

    public Map<String, Set<EGroumEdge>> getOutEdgesByType(EGroumNode node) {
        return getEdgesByType(node, edge -> getEdgeSource(edge) == node);
    }

    private Map<String, Set<EGroumEdge>> getEdgesByType(EGroumNode node, Predicate<EGroumEdge> condition) {
        Map<String, Set<EGroumEdge>> inEdgesByType = new HashMap<>();
        for (EGroumEdge edge : edgesOf(node)) {
            if (condition.test(edge)) {
                String edgeType = edge.getLabel();
                if (!inEdgesByType.containsKey(edgeType)) {
                    inEdgesByType.put(edgeType, new HashSet<>());
                }
                inEdgesByType.get(edgeType).add(edge);
            }
        }
        return inEdgesByType;
    }
}
