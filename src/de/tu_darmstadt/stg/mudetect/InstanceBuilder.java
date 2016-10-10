package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.HashMap;
import java.util.Map;

class InstanceBuilder {
    private final AUG target;
    private final AUG pattern;
    private final Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
    private final Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge = new HashMap<>();

    public InstanceBuilder(AUG target, AUG pattern) {
        this.target = target;
        this.pattern = pattern;
    }

    public AUG getTarget() {
        return target;
    }

    public AUG getPattern() {
        return pattern;
    }

    public boolean isMapped(EGroumNode targetNode, EGroumNode patternNode) {
        return targetNodeByPatternNode.get(patternNode) == targetNode;
    }

    public boolean isCompatibleMappingExtension(EGroumNode targetNode, EGroumNode patterNode) {
        return !targetNodeByPatternNode.containsValue(targetNode) &&
                !targetNodeByPatternNode.containsKey(patterNode);
    }

    public void map(EGroumNode targetNode, EGroumNode patternNode) {
        targetNodeByPatternNode.put(patternNode, targetNode);
    }

    public void map(EGroumEdge targetEdge, EGroumEdge patternEdge) {
        targetEdgeByPatternEdge.put(patternEdge, targetEdge);
    }

    public Instance build() {
        return new Instance(pattern, target, targetNodeByPatternNode, targetEdgeByPatternEdge);
    }
}
