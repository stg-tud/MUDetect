package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class InstanceBuilder {
    private final AUG target;
    private final Pattern pattern;
    private final Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
    private final Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge = new HashMap<>();

    public InstanceBuilder(AUG target, Pattern pattern) {
        this.target = target;
        this.pattern = pattern;
    }

    public AUG getTarget() {
        return target;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public boolean isMapped(EGroumNode targetNode, EGroumNode patternNode) {
        return targetNodeByPatternNode.get(patternNode) == targetNode;
    }

    public boolean isCompatibleMappingExtension(EGroumNode targetNode, EGroumNode patterNode) {
        return !targetNodeByPatternNode.containsValue(targetNode) &&
                !targetNodeByPatternNode.containsKey(patterNode);
    }

    public boolean isUnmappedTargetEdge(EGroumEdge targetEdge) {
        return !targetEdgeByPatternEdge.values().contains(targetEdge);
    }

    public void map(EGroumNode targetNode, EGroumNode patternNode) {
        targetNodeByPatternNode.put(patternNode, targetNode);
    }

    public EGroumNode getMappedTargetNode(EGroumNode patternNode) {
        return targetNodeByPatternNode.get(patternNode);
    }

    public Set<EGroumNode> getMappedTargetNodes() {
        return new HashSet<>(targetNodeByPatternNode.values());
    }

    public void map(EGroumEdge targetEdge, EGroumEdge patternEdge) {
        targetEdgeByPatternEdge.put(patternEdge, targetEdge);
    }

    public InstanceBuilder copy() {
        InstanceBuilder clone = new InstanceBuilder(target, pattern);
        clone.targetNodeByPatternNode.putAll(targetNodeByPatternNode);
        clone.targetEdgeByPatternEdge.putAll(targetEdgeByPatternEdge);
        return clone;
    }

    public Instance build() {
        return new Instance(pattern, target, targetNodeByPatternNode, targetEdgeByPatternEdge);
    }
}
