package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import egroum.EGroumDataEdge;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.HashMap;
import java.util.Map;

class InstanceBuilder {
    private TestAUGBuilder targetAUGBuilder;
    private TestAUGBuilder patternAUGBuilder;

    public static InstanceBuilder createInstance(TestAUGBuilder targetAUGBuilder, TestAUGBuilder patternAUGBuilder) {
        return new InstanceBuilder(targetAUGBuilder, patternAUGBuilder);
    }

    private final Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
    private final Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge = new HashMap<>();

    private InstanceBuilder(TestAUGBuilder targetAUGBuilder, TestAUGBuilder patternAUGBuilder) {
        this.targetAUGBuilder = targetAUGBuilder;
        this.patternAUGBuilder = patternAUGBuilder;
    }

    public InstanceBuilder withNode(String targetNodeId, String patternNodeId) {
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

    public InstanceBuilder withEdge(String targetSourceNodeId, String patternSourceNodeId, EGroumDataEdge.Type type, String targetTargetNodeId, String patternTargetNodeId) {
        targetEdgeByPatternEdge.put(
                patternAUGBuilder.getEdge(patternSourceNodeId, type, patternTargetNodeId),
                targetAUGBuilder.getEdge(targetSourceNodeId, type, targetTargetNodeId));
        return this;
    }

    public Instance build() {
        return new Instance(patternAUGBuilder.build(), targetAUGBuilder.build(), targetNodeByPatternNode, targetEdgeByPatternEdge);
    }
}
