package de.tu_darmstadt.stg.mudetect.model;

import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.Set;
import java.util.stream.Collectors;

public class AUG extends DirectedMultigraph<EGroumNode, EGroumEdge> {

    private final Location location;

    private Set<EGroumNode> meaningfullActionNodesCache = null;

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

    public Set<EGroumNode> getMeaningfulActionNodes() {
        if (meaningfullActionNodesCache == null) {
            meaningfullActionNodesCache = vertexSet().stream()
                    .filter(EGroumNode::isMeaningfulAction).collect(Collectors.toSet());
        }
        return meaningfullActionNodesCache;
    }

    public Set<String> getAPIs() {
        return vertexSet().stream().filter(EGroumNode::isAPI).map(EGroumNode::getLabel).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "AUG{" +
                "location=" + location +
                ", aug=" + super.toString() +
                '}';
    }
}
