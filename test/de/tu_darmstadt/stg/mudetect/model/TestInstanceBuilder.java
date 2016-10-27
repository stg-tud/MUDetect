package de.tu_darmstadt.stg.mudetect.model;

import egroum.EGroumDataEdge;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;

public class TestInstanceBuilder {

    public static Instance someInstance() {
        return fullInstance(someAUG());
    }

    public static Instance emptyInstance(Pattern pattern) {
        return emptyInstance(pattern, pattern);
    }

    public static Instance emptyInstance(AUG aug) {
        return emptyInstance(somePattern(aug), aug);
    }

    public static Instance fullInstance(AUG aug) {
        return someInstance(aug, aug.vertexSet(), aug.edgeSet());
    }

    public static Instance someInstance(AUG pattern, Set<EGroumNode> vertexSubset, Set<EGroumEdge> edgeSubset) {
        Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
        for (EGroumNode node : vertexSubset) {
            targetNodeByPatternNode.put(node, node);
        }
        Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge = new HashMap<>();
        for (EGroumEdge edge : edgeSubset) {
            targetEdgeByPatternEdge.put(edge, edge);
        }
        return new Instance(somePattern(pattern), pattern, targetNodeByPatternNode, targetEdgeByPatternEdge);
    }

    public static Instance emptyInstance(Pattern pattern, AUG target) {
        return new Instance(pattern, target, new HashMap<>(), new HashMap<>());
    }

    public static Instance someInstance(Pattern pattern, AUG target) {
        return emptyInstance(pattern, target);
    }

    public static TestInstanceBuilder buildInstance(TestAUGBuilder targetAUGBuilder, TestAUGBuilder patternAUGBuilder) {
        return new TestInstanceBuilder(targetAUGBuilder, patternAUGBuilder);
    }

    public static TestInstanceBuilder buildInstance(TestAUGBuilder targetAndPatternAUGBuilder) {
        return buildInstance(targetAndPatternAUGBuilder, targetAndPatternAUGBuilder);
    }

    private final TestAUGBuilder targetAUGBuilder;
    private final TestAUGBuilder patternAUGBuilder;
    private final Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
    private final Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge = new HashMap<>();

    private TestInstanceBuilder(TestAUGBuilder targetAUGBuilder, TestAUGBuilder patternAUGBuilder) {
        this.targetAUGBuilder = targetAUGBuilder;
        this.patternAUGBuilder = patternAUGBuilder;
    }

    public TestInstanceBuilder withNode(String targetAndPatternNodeId) {
        return withNode(targetAndPatternNodeId, targetAndPatternNodeId);
    }

    public TestInstanceBuilder withNodes(String... targetAndPatternNodeIds) {
        for (String targetAndPatternNodeId : targetAndPatternNodeIds) {
            withNode(targetAndPatternNodeId);
        }
        return this;
    }

    public TestInstanceBuilder withNode(String targetNodeId, String patternNodeId) {
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

    public TestInstanceBuilder withEdge(String targetSourceNodeId, String patternSourceNodeId, EGroumDataEdge.Type type, String targetTargetNodeId, String patternTargetNodeId) {
        targetEdgeByPatternEdge.put(
                patternAUGBuilder.getEdge(patternSourceNodeId, type, patternTargetNodeId),
                targetAUGBuilder.getEdge(targetSourceNodeId, type, targetTargetNodeId));
        return this;
    }

    public TestInstanceBuilder withEdge(String targetAndPatternSourceNodeId, EGroumDataEdge.Type type, String targetAndPatternTargetNodeId) {
        return withEdge(targetAndPatternSourceNodeId, targetAndPatternSourceNodeId, type, targetAndPatternTargetNodeId, targetAndPatternTargetNodeId);
    }

    public Instance build() {
        return new Instance(
                somePattern(patternAUGBuilder), targetAUGBuilder.build(),
                targetNodeByPatternNode, targetEdgeByPatternEdge);
    }
}
