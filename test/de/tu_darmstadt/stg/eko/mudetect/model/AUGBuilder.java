package de.tu_darmstadt.stg.eko.mudetect.model;

import egroum.*;

import java.util.HashMap;
import java.util.Map;

public class AUGBuilder {
    private int autoId = 0;
    private Map<String, EGroumNode> nodeMap = new HashMap<>();
    private AUG aug = new AUG();

    public static AUGBuilder newAUG() {
        return new AUGBuilder();
    }

    public AUGBuilder withActionNodes(String... nodeNames) {
        for (String nodeName : nodeNames) {
            withActionNode(nodeName);
        }
        return this;
    }

    public AUGBuilder withActionNode(String nodeName) {
        if (nodeMap.containsKey(nodeName)) {
            throw new IllegalArgumentException("A node with id '" + nodeName + "' already exists, please specify an explicit node id.");
        }
        return withActionNode(nodeName, nodeName);
    }

    public AUGBuilder withActionNode(String id, String nodeName) {
        return withNode(id, new EGroumActionNode(nodeName));
    }

    public AUGBuilder withDataNode(String nodeName) {
        if (nodeMap.containsKey(nodeName)) {
            throw new IllegalArgumentException("A node with id '" + nodeName + "' already exists, please specify an explicit node id.");
        }
        return withDataNode(nodeName, nodeName);
    }

    public AUGBuilder withDataNode(String id, String nodeName) {
        return withNode(id, new EGroumDataNode(nodeName));
    }

    public AUGBuilder withNode(String id, EGroumNode node) {
        if (nodeMap.containsKey(id)) {
            throw new IllegalArgumentException("A node with id '" + id + "' already exists.");
        }
        nodeMap.put(id, node);
        aug.addVertex(node);
        return this;
    }

    private AUGBuilder withNodes(EGroumNode... nodes) {
        for (EGroumNode node : nodes) {
            withNode(getNextAutoId(), node);
        }
        return this;
    }

    private String getNextAutoId() {
        return Integer.toString(autoId++);
    }

    public AUGBuilder withEdge(EGroumEdge edge) {
        aug.addEdge(edge.getSource(), edge.getTarget(), edge);
        return this;
    }

    public AUGBuilder withDataEdge(String sourceId, EGroumDataEdge.Type type, String targetId) {
        return withEdge(new EGroumDataEdge(getNode(sourceId), getNode(targetId), type));
    }

    private EGroumNode getNode(String id) {
        if (!nodeMap.containsKey(id)) {
            throw new IllegalArgumentException("A node with id '" + id + "' does not exist.");
        }
        return nodeMap.get(id);
    }

    public AUG build() {
        return aug;
    }
}
