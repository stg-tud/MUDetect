package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.Instance;
import egroum.EGroumDataEdge;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.HashMap;
import java.util.Map;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;

public class TestInstanceBuilder {

    public static Instance someInstance() {
        final AUG aug = someAUG();
        return new Instance(aug, aug.vertexSet(), aug.edgeSet());
    }

    public static Instance someInstance(AUG pattern, AUG target) {
        return emptyInstance(pattern, target);
    }

    public static Instance emptyInstance(AUG pattern, AUG target) {
        return new Instance(pattern, target, new HashMap<>(), new HashMap<>());
    }

    public static TestInstanceBuilder buildInstance(TestAUGBuilder targetAUGBuilder, TestAUGBuilder patternAUGBuilder) {
        return new TestInstanceBuilder(targetAUGBuilder, patternAUGBuilder);
    }

    private final TestAUGBuilder targetAUGBuilder;
    private final TestAUGBuilder patternAUGBuilder;
    private final Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
    private final Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge = new HashMap<>();

    private TestInstanceBuilder(TestAUGBuilder targetAUGBuilder, TestAUGBuilder patternAUGBuilder) {
        this.targetAUGBuilder = targetAUGBuilder;
        this.patternAUGBuilder = patternAUGBuilder;
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

    public Instance build() {
        return new Instance(patternAUGBuilder.build(), targetAUGBuilder.build(), targetNodeByPatternNode, targetEdgeByPatternEdge);
    }
}
