package de.tu_darmstadt.stg.mudetect.model;

import egroum.*;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AUGBuilder {
    private int autoId = 0;

    private final Map<String, EGroumNode> nodeMap;
    private final Set<EGroumEdge> edges;
    private final String name;

    private AUGBuilder(String name) {
        this.name = name;
        nodeMap = new HashMap<>();
        edges = new HashSet<>();
    }

    private AUGBuilder(AUGBuilder baseBuilder) {
        name = baseBuilder.name;
        nodeMap = new HashMap<>(baseBuilder.nodeMap);
        edges = new HashSet<>(baseBuilder.edges);
    }

    public static AUG someAUG() {
        return buildAUG().withActionNode(":dummy:").build();
    }

    public static AUGBuilder buildAUG() {
        return new AUGBuilder(":AUG:");
    }

    public static AUGBuilder buildAUG(String name) {
        return new AUGBuilder(name);
    }

    public static AUGBuilder extend(AUGBuilder baseBuilder) {return new AUGBuilder(baseBuilder); }

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
        int nodeType;
        if (EGroumNode.infixExpressionLables.containsKey(nodeName)) {
            nodeName = Character.toString(EGroumNode.infixExpressionLables.get(nodeName));
            nodeType = ASTNode.INFIX_EXPRESSION;
        } else {
            nodeType = ASTNode.METHOD_INVOCATION;
        }
        return withNode(id, new EGroumActionNode(nodeName, nodeType));
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

    public AUGBuilder withDataEdge(String sourceId, EGroumDataEdge.Type type, String targetId) {
        edges.add(new EGroumDataEdge(getNode(sourceId), getNode(targetId), type));
        return this;
    }

    public EGroumNode getNode(String id) {
        if (!nodeMap.containsKey(id)) {
            throw new IllegalArgumentException("A node with id '" + id + "' does not exist.");
        }
        return nodeMap.get(id);
    }

    public EGroumEdge getEdge(String sourceNodeId, EGroumDataEdge.Type type, String targetNodeId) {
        for (EGroumEdge edge : edges) {
            if (edge.getSource() == getNode(sourceNodeId) &&
                    edge.getTarget() == getNode(targetNodeId) &&
                    edge.getLabel().equals(EGroumDataEdge.getLabel(type))) {
                return edge;
            }
        }
        throw new IllegalArgumentException("no such edge");
    }

    public AUG build() {
        AUG aug = new AUG(name, ":aug-file-path:");
        for (EGroumNode node : nodeMap.values()) {
            aug.addVertex(node);
        }
        for (EGroumEdge edge : edges) {
            aug.addEdge(edge.getSource(), edge.getTarget(), edge);
        }
        return aug;
    }
}
