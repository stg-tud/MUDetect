package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.aug.model.*;
import de.tu_darmstadt.stg.mudetect.aug.model.Location;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.InfixOperatorNode;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.ReturnNode;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.RepetitionEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.SelectionEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.data.VariableNode;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.BaseDataFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.utils.JavaASTUtil;

import java.util.*;

public class TestAUGBuilder {
    private static int randomAUGCount = 0;

    private final Map<String, Node> nodeMap;
    private final Set<Edge> edges;
    private final String name;

    private TestAUGBuilder(String name) {
        this.name = name;
        nodeMap = new LinkedHashMap<>();
        edges = new LinkedHashSet<>();
    }

    private TestAUGBuilder(TestAUGBuilder baseBuilder) {
        name = baseBuilder.name;
        nodeMap = new LinkedHashMap<>(baseBuilder.nodeMap);
        edges = new LinkedHashSet<>(baseBuilder.edges);
    }

    public static APIUsageExample someAUG() {
        return someAUG(getFreshAUGName());
    }

    private static String getFreshAUGName() {
        return ":AUG-" + (++randomAUGCount) + ":";
    }

    public static APIUsageExample someAUG(String name) {
        return buildAUG(name).withActionNode(getFreshNodeName()).build(APIUsageExample.class);
    }

    private static String getFreshNodeName() {
        return ":node-" + (++randomAUGCount) + ":";
    }

    public static TestAUGBuilder buildAUG() {
        return new TestAUGBuilder(getFreshAUGName());
    }

    public static TestAUGBuilder buildAUG(String name) {
        return new TestAUGBuilder(name);
    }

    private static TestAUGBuilder join(TestAUGBuilder... builder) {
        TestAUGBuilder joinedBuilder = null;
        for (int i = 0; i < builder.length; i++) {
            if (i == 0) {
                joinedBuilder = new TestAUGBuilder(builder[0]);
            } else {
                joinedBuilder.nodeMap.putAll(builder[i].nodeMap);
                joinedBuilder.edges.addAll(builder[i].edges);
            }
        }
        return joinedBuilder;
    }

    public static TestAUGBuilder extend(TestAUGBuilder... baseBuilder) {return new TestAUGBuilder(join(baseBuilder)); }

    static TestAUGBuilder builderFrom(APIUsageExample aug) {
        return builderFrom(aug, aug.getLocation().getMethodSignature());
    }

    static TestAUGBuilder builderFrom(APIUsageGraph aug) {
        return builderFrom(aug, "graph");
    }

    private static TestAUGBuilder builderFrom(APIUsageGraph aug, String name) {
        TestAUGBuilder builder = new TestAUGBuilder(name);
        for (Node node : aug.vertexSet()) {
            builder.withNode(node.getLabel(), node);
        }
        builder.edges.addAll(aug.edgeSet());
        return builder;
    }

    public TestAUGBuilder withActionNodes(String... nodeNames) {
        for (String nodeName : nodeNames) {
            withActionNode(nodeName);
        }
        return this;
    }

    public TestAUGBuilder withActionNode(String nodeName) {
        if (nodeMap.containsKey(nodeName)) {
            throw new IllegalArgumentException("A node with id '" + nodeName + "' already exists, please specify an explicit node id.");
        }
        return withActionNode(nodeName, nodeName);
    }

    public TestAUGBuilder withActionNode(String id, final String nodeName) {
        if (JavaASTUtil.infixExpressionLables.containsKey(nodeName)) {
            String abstractOperatorName = JavaASTUtil.infixExpressionLables.get(nodeName);
            return withNode(id, new InfixOperatorNode(abstractOperatorName));
        } else if (nodeName.equals("return")) {
            return withNode(id, new ReturnNode());
        } else {
            if (nodeName.contains(".")) {
                String[] nameParts = nodeName.split("\\.");
                return withNode(id, new MethodCallNode(nameParts[0], nameParts[1]));
            } else {
                return withNode(id, new MethodCallNode("", nodeName) {
                    @Override
                    public String getLabel() {
                        return nodeName;
                    }
                });
            }
        }
    }

    public TestAUGBuilder withDataNodes(String... nodeNames) {
        for (String nodeName : nodeNames) {
            withDataNode(nodeName);
        }
        return this;
    }

    public TestAUGBuilder withDataNode(String nodeName) {
        if (nodeMap.containsKey(nodeName)) {
            throw new IllegalArgumentException("A node with id '" + nodeName + "' already exists, please specify an explicit node id.");
        }
        return withDataNode(nodeName, nodeName);
    }

    public TestAUGBuilder withDataNode(String id, String nodeName) {
        // TODO check whether we need the second parameter here
        return withNode(id, new VariableNode(nodeName, null));
    }

    public TestAUGBuilder withNode(String id, Node node) {
        if (nodeMap.containsKey(id)) {
            throw new IllegalArgumentException("A node with id '" + id + "' already exists.");
        }
        nodeMap.put(id, node);
        return this;
    }

    public TestAUGBuilder withDataEdge(String sourceId, Edge.Type type, String targetId) {
        edges.add(new BaseDataFlowEdge(getNode(sourceId), getNode(targetId), type));
        return this;
    }

    public TestAUGBuilder withCondEdge(String sourceId, ConditionEdge.ConditionType kind, String targetId) {
        switch(kind) {
            case SELECTION:
                edges.add(new SelectionEdge(getNode(sourceId), getNode(targetId)));
                break;
            case REPETITION:
                edges.add(new RepetitionEdge(getNode(sourceId), getNode(targetId)));
                break;
        }
        return this;
    }

    Node getNode(String id) {
        if (!nodeMap.containsKey(id)) {
            throw new IllegalArgumentException("A node with id '" + id + "' does not exist.");
        }
        return nodeMap.get(id);
    }

    Edge getEdge(String sourceNodeId, Edge.Type type, String targetNodeId) {
        for (Edge edge : edges) {
            if (edge.getSource() == getNode(sourceNodeId) &&
                    edge.getTarget() == getNode(targetNodeId) &&
                    hasType(edge, type)) {
                return edge;
            }
        }
        throw new IllegalArgumentException("no such edge");
    }

    private boolean hasType(Edge edge, Edge.Type type) {
        return edge.getType() == type;
    }

    public APIUsageExample build() {
        return build(APIUsageExample.class);
    }

    public <T extends APIUsageGraph> T build(Class<T> clazz) {
        APIUsageGraph aug;
        if (clazz == APIUsageExample.class) {
            aug = new APIUsageExample(new Location(name, ":aug-file-path:", name));
        } else if (clazz == APIUsagePattern.class) {
            aug = new APIUsagePattern(42, new HashSet<>());
        } else {
            throw new IllegalArgumentException("unsupported AUG type: " + clazz);
        }

        for (Node node : nodeMap.values()) {
            aug.addVertex(node);
        }
        for (Edge edge : edges) {
            aug.addEdge(edge.getSource(), edge.getTarget(), edge);
        }
        return (T) aug;
    }
}
