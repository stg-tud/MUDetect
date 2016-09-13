package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import egroum.EGroumActionNode;
import egroum.EGroumDataNode;
import egroum.EGroumNode;

import java.util.*;

public class InstanceFinder {
    private static class CommonNode<T extends EGroumNode> {
        private T targetNode;
        private T patternNode;

        CommonNode(T targetNode, T patternNode) {
            this.targetNode = targetNode;
            this.patternNode = patternNode;
        }
    }

    public static List<Instance> findInstances(AUG target, AUG pattern) {
        Map<String, Set<EGroumActionNode>> patternNodesByLabel = getMeaningfulActionNodesByLabel(pattern);

        Set<CommonNode<EGroumActionNode>> initialNodes = new HashSet<>();
        for (EGroumNode targetNode : target.vertexSet()) {
            String label = targetNode.getLabel();
            if (patternNodesByLabel.containsKey(label)) {
                for (EGroumActionNode patternNode : patternNodesByLabel.get(label)) {
                    initialNodes.add(new CommonNode<>((EGroumActionNode) targetNode, patternNode));
                }

            }
        }

        List<Instance> instances = new ArrayList<>();
        while (!initialNodes.isEmpty()) {
            CommonNode<EGroumActionNode> node = poll(initialNodes);
            Instance instance = new Instance(pattern, new HashSet<>());
            extendInstance(instance, target, node.targetNode, pattern, node.patternNode, initialNodes);
            instances.add(instance);
        }

        return instances;
    }

    private static CommonNode<EGroumActionNode> poll(Set<CommonNode<EGroumActionNode>> initialNodes) {
        Iterator<CommonNode<EGroumActionNode>> iterator = initialNodes.iterator();
        CommonNode<EGroumActionNode> node = iterator.next();
        iterator.remove();
        return node;
    }

    private static void extendInstance(Instance instance, AUG target, EGroumActionNode targetNode, AUG pattern, EGroumActionNode patternNode, Set<CommonNode<EGroumActionNode>> initialNodes) {
        instance.addVertex(patternNode);

        EGroumDataNode patternReceiver = pattern.getReceiver(patternNode);
        if (patternReceiver != null) {
            EGroumDataNode targetReceiver = target.getReceiver(targetNode);
            if (targetReceiver.getLabel().equals(patternReceiver.getLabel())) {
                instance.addVertex(patternReceiver);
                instance.addEdge(patternReceiver, patternNode, pattern.getEdge(patternReceiver, patternNode));
                extendInstance(instance, target, targetReceiver, pattern, patternReceiver, initialNodes);
            }
        }

        Set<EGroumActionNode> patternConditions = pattern.getConditions(patternNode);
        Set<EGroumActionNode> targetConditions = target.getConditions(targetNode);
        for (EGroumActionNode patternCondition : patternConditions) {
            for (EGroumActionNode targetCondition : targetConditions) {
                if (patternCondition.getLabel().equals(targetCondition.getLabel())) {
                    instance.addVertex(patternCondition);
                    instance.addEdge(patternCondition, patternNode, pattern.getEdge(patternCondition, patternNode));
                    extendInstance(instance, target, targetCondition, pattern, patternCondition, initialNodes);
                    for (Iterator<CommonNode<EGroumActionNode>> it = initialNodes.iterator(); it.hasNext();) {
                        CommonNode<EGroumActionNode> node = it.next();
                        if (node.patternNode == patternCondition) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    private static void extendInstance(Instance instance, AUG target, EGroumDataNode targetNode, AUG pattern, EGroumDataNode patternNode, Set<CommonNode<EGroumActionNode>> initialNodes) {
        Set<EGroumActionNode> patternInvocations = pattern.getInvocations(patternNode);
        Set<EGroumActionNode> targetInvocations = target.getInvocations(targetNode);
        for (EGroumActionNode patternInvocation : patternInvocations) {
            if (!instance.containsVertex(patternInvocation)) {
                for (EGroumActionNode targetInvocation : targetInvocations) {
                    if (targetInvocation.getLabel().equals(patternInvocation.getLabel())) {
                        instance.addVertex(patternInvocation);
                        instance.addEdge(patternNode, patternInvocation, pattern.getEdge(patternNode, patternInvocation));
                        extendInstance(instance, target, targetInvocation, pattern, patternInvocation, initialNodes);

                        for (Iterator<CommonNode<EGroumActionNode>> it = initialNodes.iterator(); it.hasNext();) {
                            CommonNode<EGroumActionNode> node = it.next();
                            if (node.patternNode == patternInvocation) {
                                it.remove();
                            }
                        }
                    }
                }

            }
        }

    }

    private static Map<String, Set<EGroumActionNode>> getMeaningfulActionNodesByLabel(AUG aug) {
        Map<String, Set<EGroumActionNode>> nodesByLabel = new HashMap<>();
        for (EGroumNode node : aug.vertexSet()) {
            if (node.isMeaningfulAction()) {
                String label = node.getLabel();
                if (!nodesByLabel.containsKey(label)) {
                    nodesByLabel.put(label, new HashSet<>());
                }
                nodesByLabel.get(label).add((EGroumActionNode) node);
            }
        }
        return nodesByLabel;
    }

}
