package de.tu_darmstadt.stg.mudetect.model;

import egroum.EGroumDataEdge;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;

public class TestOverlapBuilder {

    public static Overlap someOverlap() {
        return instance(someAUG());
    }

    public static Overlap emptyOverlap(Pattern pattern) {
        return emptyOverlap(pattern, pattern);
    }

    public static Overlap emptyOverlap(AUG aug) {
        return emptyOverlap(somePattern(aug), aug);
    }

    public static Overlap instance(AUG aug) {
        return someOverlap(aug, aug.vertexSet(), aug.edgeSet());
    }

    public static Overlap someOverlap(AUG pattern, Set<EGroumNode> vertexSubset, Set<EGroumEdge> edgeSubset) {
        Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
        for (EGroumNode node : vertexSubset) {
            targetNodeByPatternNode.put(node, node);
        }
        Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge = new HashMap<>();
        for (EGroumEdge edge : edgeSubset) {
            targetEdgeByPatternEdge.put(edge, edge);
        }
        return new Overlap(somePattern(pattern), pattern, targetNodeByPatternNode, targetEdgeByPatternEdge);
    }

    public static Overlap emptyOverlap(Pattern pattern, AUG target) {
        return new Overlap(pattern, target, new HashMap<>(), new HashMap<>());
    }

    public static Overlap someOverlap(Pattern pattern, AUG target) {
        return emptyOverlap(pattern, target);
    }

    public static TestOverlapBuilder buildOverlap(TestAUGBuilder targetAUGBuilder, TestAUGBuilder patternAUGBuilder) {
        return new TestOverlapBuilder(targetAUGBuilder, patternAUGBuilder);
    }

    public static TestOverlapBuilder buildOverlap(TestAUGBuilder targetAndPatternAUGBuilder) {
        return buildOverlap(targetAndPatternAUGBuilder, targetAndPatternAUGBuilder);
    }

    public static Overlap instance(TestAUGBuilder targetAndPatternAUGBuilder) {
        return instance(targetAndPatternAUGBuilder.build());
    }

    private final TestAUGBuilder targetAUGBuilder;
    private final TestAUGBuilder patternAUGBuilder;
    private final Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
    private final Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge = new HashMap<>();

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
        EGroumNode targetNode = targetAUGBuilder.getNode(targetNodeId);
        if (targetNodeByPatternNode.containsValue(targetNode)) {
            throw new IllegalArgumentException("Target node '" + targetNodeId + "' is already mapped.");
        }
        EGroumNode patternNode = patternAUGBuilder.getNode(patternNodeId);
        if (targetNodeByPatternNode.containsKey(patternNode)) {
            throw new IllegalArgumentException("Pattern node '" + patternNodeId + "' is already mapped.");
        }
        targetNodeByPatternNode.put(patternNode, targetNode);
        return this;
    }

    public TestOverlapBuilder withEdge(String targetSourceNodeId, String patternSourceNodeId, EGroumDataEdge.Type type, String targetTargetNodeId, String patternTargetNodeId) {
        targetEdgeByPatternEdge.put(
                patternAUGBuilder.getEdge(patternSourceNodeId, type, patternTargetNodeId),
                targetAUGBuilder.getEdge(targetSourceNodeId, type, targetTargetNodeId));
        return this;
    }

    public TestOverlapBuilder withEdge(String targetAndPatternSourceNodeId, EGroumDataEdge.Type type, String targetAndPatternTargetNodeId) {
        return withEdge(targetAndPatternSourceNodeId, targetAndPatternSourceNodeId, type, targetAndPatternTargetNodeId, targetAndPatternTargetNodeId);
    }

    public Overlap build() {
        return new Overlap(
                somePattern(patternAUGBuilder), targetAUGBuilder.build(),
                targetNodeByPatternNode, targetEdgeByPatternEdge);
    }
}
