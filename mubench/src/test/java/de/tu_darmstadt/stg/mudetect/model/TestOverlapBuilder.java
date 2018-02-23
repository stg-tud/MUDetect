package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.aug.model.*;
import de.tu_darmstadt.stg.mudetect.aug.model.Location;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.someAUG;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.builderFrom;
import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.somePattern;

public class TestOverlapBuilder {

    public static Overlap someOverlap() {
        return instance(someAUG());
    }

    public static Overlap emptyOverlap(APIUsagePattern pattern) {
        return emptyOverlap(pattern, someAUG());
    }

    public static Overlap emptyOverlap(APIUsageExample example) {
        return emptyOverlap(somePattern(example), example);
    }

    public static Overlap emptyOverlap(TestAUGBuilder pattern) {
        return emptyOverlap(pattern.build(APIUsagePattern.class));
    }

    public static Overlap instance(APIUsageGraph aug) {
        return someOverlap(aug, aug.vertexSet(), aug.edgeSet());
    }

    public static Overlap someOverlap(APIUsageGraph pattern, Set<Node> vertexSubset, Set<Edge> edgeSubset) {
        Map<Node, Node> targetNodeByPatternNode = new HashMap<>();
        APIUsageExample target = new APIUsageExample(new Location("test", "/test", "test()"));
        for (Node node : vertexSubset) {
            targetNodeByPatternNode.put(node, node);
            target.addVertex(node);
        }
        Map<Edge, Edge> targetEdgeByPatternEdge = new HashMap<>();
        for (Edge edge : edgeSubset) {
            targetEdgeByPatternEdge.put(edge, edge);
            target.addEdge(edge.getSource(), edge.getTarget(), edge);
        }
        return new Overlap(somePattern(pattern), target, targetNodeByPatternNode, targetEdgeByPatternEdge);
    }

    public static Overlap emptyOverlap(APIUsagePattern pattern, APIUsageExample target) {
        return new Overlap(pattern, target, new HashMap<>(), new HashMap<>());
    }

    public static Overlap someOverlap(APIUsagePattern pattern, APIUsageExample target) {
        HashMap<Node, Node> targetNodeByPatternNode = new HashMap<>();
        Set<Node> patternNodes = pattern.vertexSet();
        if (patternNodes.isEmpty()) {
            throw new IllegalArgumentException("need at least one pattern node to build some overlap");
        }
        Set<Node> targetNodes = target.vertexSet();
        if (targetNodes.isEmpty()) {
            throw new IllegalArgumentException("need at least one target node to build some overlap");
        }
        targetNodeByPatternNode.put(patternNodes.iterator().next(), targetNodes.iterator().next());
        return new Overlap(pattern, target, targetNodeByPatternNode, new HashMap<>());
    }

    public static Overlap someOverlap(TestAUGBuilder patternAUGBuilder, TestAUGBuilder targetAUGBuilder) {
        return someOverlap(somePattern(patternAUGBuilder), targetAUGBuilder.build());
    }

    public static TestOverlapBuilder buildOverlap(TestAUGBuilder patternAUGBuilder, TestAUGBuilder targetAUGBuilder) {
        return new TestOverlapBuilder(targetAUGBuilder, patternAUGBuilder);
    }

    public static TestOverlapBuilder buildOverlap(TestAUGBuilder targetAndPatternAUGBuilder) {
        return buildOverlap(targetAndPatternAUGBuilder, targetAndPatternAUGBuilder);
    }

    public static TestOverlapBuilder buildOverlap(APIUsagePattern pattern, APIUsageExample target) {
        return buildOverlap(builderFrom(pattern), builderFrom(target));
    }

    public static Overlap instance(TestAUGBuilder targetAndPatternAUGBuilder) {
        return instance(targetAndPatternAUGBuilder.build());
    }

    private final TestAUGBuilder targetAUGBuilder;
    private final TestAUGBuilder patternAUGBuilder;
    private final Map<Node, Node> targetNodeByPatternNode = new HashMap<>();
    private final Map<Edge, Edge> targetEdgeByPatternEdge = new HashMap<>();

    private TestOverlapBuilder(TestAUGBuilder targetAUGBuilder, TestAUGBuilder patternAUGBuilder) {
        this.targetAUGBuilder = targetAUGBuilder;
        this.patternAUGBuilder = patternAUGBuilder;
    }

    public TestOverlapBuilder withNode(String targetAndPatternNodeId) {
        return withNode(targetAndPatternNodeId, targetAndPatternNodeId);
    }

    public TestOverlapBuilder withNodes(String... targetAndPatternNodeIds) {
        for (String targetAndPatternNodeId : targetAndPatternNodeIds) {
            withNode(targetAndPatternNodeId);
        }
        return this;
    }

    public TestOverlapBuilder withNode(String targetNodeId, String patternNodeId) {
        Node targetNode = targetAUGBuilder.getNode(targetNodeId);
        if (targetNodeByPatternNode.containsValue(targetNode)) {
            throw new IllegalArgumentException("Target node '" + targetNodeId + "' is already mapped.");
        }
        Node patternNode = patternAUGBuilder.getNode(patternNodeId);
        if (targetNodeByPatternNode.containsKey(patternNode)) {
            throw new IllegalArgumentException("Pattern node '" + patternNodeId + "' is already mapped.");
        }
        targetNodeByPatternNode.put(patternNode, targetNode);
        return this;
    }

    public TestOverlapBuilder withEdge(String targetSourceNodeId, String patternSourceNodeId, Edge.Type type, String targetTargetNodeId, String patternTargetNodeId) {
        if (!maps(targetSourceNodeId, patternSourceNodeId)) {
            throw new IllegalArgumentException("not mapped '" + targetSourceNodeId + "'<->'" + patternSourceNodeId + "'");
        }
        if (!maps(targetTargetNodeId, patternTargetNodeId)) {
            throw new IllegalArgumentException("not mapped '" + targetTargetNodeId + "'<->'" + patternTargetNodeId + "'");
        }
        targetEdgeByPatternEdge.put(
                patternAUGBuilder.getEdge(patternSourceNodeId, type, patternTargetNodeId),
                targetAUGBuilder.getEdge(targetSourceNodeId, type, targetTargetNodeId));
        return this;
    }

    public TestOverlapBuilder withEdge(String targetSourceNodeId, String patternSourceNodeId, ConditionEdge.ConditionType type, String targetTargetNodeId, String patternTargetNodeId) {
        if (!maps(targetSourceNodeId, patternSourceNodeId)) {
            throw new IllegalArgumentException("not mapped '" + targetSourceNodeId + "'<->'" + patternSourceNodeId + "'");
        }
        if (!maps(targetTargetNodeId, patternTargetNodeId)) {
            throw new IllegalArgumentException("not mapped '" + targetTargetNodeId + "'<->'" + patternTargetNodeId + "'");
        }
        targetEdgeByPatternEdge.put(
                patternAUGBuilder.getEdge(patternSourceNodeId, type, patternTargetNodeId),
                targetAUGBuilder.getEdge(targetSourceNodeId, type, targetTargetNodeId));
        return this;
    }

    private boolean maps(String targetNodeId, String patternNodeId) {
        return targetNodeByPatternNode.get(patternAUGBuilder.getNode(patternNodeId)) == targetAUGBuilder.getNode(targetNodeId);
    }

    public TestOverlapBuilder withEdge(String targetAndPatternSourceNodeId, Edge.Type type, String targetAndPatternTargetNodeId) {
        return withEdge(targetAndPatternSourceNodeId, targetAndPatternSourceNodeId, type, targetAndPatternTargetNodeId, targetAndPatternTargetNodeId);
    }

    public TestOverlapBuilder withEdge(String targetAndPatternSourceNodeId, ConditionEdge.ConditionType type, String targetAndPatternTargetNodeId) {
        return withEdge(targetAndPatternSourceNodeId, targetAndPatternSourceNodeId, type, targetAndPatternTargetNodeId, targetAndPatternTargetNodeId);
    }

    public Overlap build() {
        return new Overlap(
                somePattern(patternAUGBuilder), targetAUGBuilder.build(),
                targetNodeByPatternNode, targetEdgeByPatternEdge);
    }
}
