package de.tu_darmstadt.stg.mudetect.model;

import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AUG extends DirectedMultigraph<EGroumNode, EGroumEdge> {

    private final Location location;

    public AUG(String name, String filePath) {
        super(EGroumEdge.class);
        this.location = new Location(filePath, name);
    }

    public Location getLocation() {
        return location;
    }

    public int getNodeSize() {
        return vertexSet().size();
    }

    public int getEdgeSize() {
        return edgeSet().size();
    }

    public int getSize() {
        return getNodeSize() + getEdgeSize();
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

    public Map<String, Set<EGroumNode>> getMeaningfulActionNodesByLabel() {
        Map<String, Set<EGroumNode>> nodesByLabel = new HashMap<>();
        for (EGroumNode node : vertexSet()) {
            if (node.isMeaningfulAction()) {
                String label = node.getLabel();
                if (!nodesByLabel.containsKey(label)) {
                    nodesByLabel.put(label, new HashSet<>());
                }
                nodesByLabel.get(label).add(node);
            }
        }
        return nodesByLabel;
    }

    public Set<EGroumNode> getMeaningfulActionNodes() {
        return vertexSet().stream().filter(EGroumNode::isMeaningfulAction).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "AUG{" +
                "location=" + location +
                ", aug=" + super.toString() +
                '}';
    }
}
