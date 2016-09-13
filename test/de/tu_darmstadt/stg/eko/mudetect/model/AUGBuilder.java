package de.tu_darmstadt.stg.eko.mudetect.model;

import egroum.*;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AUGBuilder {
    private int autoId = 0;

    private Map<String, EGroumNode> nodeMap = new HashMap<>();
    private Set<EGroumEdge> edges = new HashSet<>();

    public static AUGBuilder buildAUG() {
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
        int nodeType;
        if (InfixExpression.Operator.toOperator(nodeName) != null) {
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

    public AUG build() {
        AUG aug = new AUG();
        for (EGroumNode node : nodeMap.values()) {
            aug.addVertex(node);
        }
        for (EGroumEdge edge : edges) {
            aug.addEdge(edge.getSource(), edge.getTarget(), edge);
        }
        return aug;
    }
}
