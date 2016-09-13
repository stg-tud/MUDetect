package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import egroum.EGroumActionNode;
import egroum.EGroumDataNode;
import egroum.EGroumGraph;
import egroum.EGroumNode;

import java.util.*;

public class InstanceFinder {
    private static class WorkItem {
        AUG pattern;
        EGroumNode patternNode;
        AUG target;
        EGroumNode targetNode;

        WorkItem(AUG pattern, EGroumNode patternNode, AUG target, EGroumNode targetNode) {
            this.pattern = pattern;
            this.patternNode = patternNode;
            this.target = target;
            this.targetNode = targetNode;
        }
    }

    private static class WorkQueue {
        private Map<EGroumNode, WorkItem> workItemsByPatternNode = new HashMap<>();

        void add(WorkItem item) {
            if (workItemsByPatternNode.containsKey(item.patternNode)) {
                throw new IllegalStateException("adding a second item for the same pattern node");
            }
            workItemsByPatternNode.put(item.patternNode, item);
        }

        boolean hasNext() {
            return !workItemsByPatternNode.isEmpty();
        }

        WorkItem poll() {
            Iterator<Map.Entry<EGroumNode, WorkItem>> iterator = workItemsByPatternNode.entrySet().iterator();
            Map.Entry<EGroumNode, WorkItem> next = iterator.next();
            workItemsByPatternNode.remove(next.getKey());
            return next.getValue();
        }

        void remove(EGroumNode patternNode) {
            workItemsByPatternNode.remove(patternNode);
        }
    }

    public static List<Instance> findInstances(AUG target, AUG pattern) {
        Map<String, Set<EGroumActionNode>> patternNodesByLabel = getMeaningfulActionNodesByLabel(pattern);

        WorkQueue queue = new WorkQueue();
        for (EGroumNode targetNode : target.vertexSet()) {
            String label = targetNode.getLabel();
            if (patternNodesByLabel.containsKey(label)) {
                for (EGroumActionNode patternNode : patternNodesByLabel.get(label)) {
                    queue.add(new WorkItem(pattern, patternNode, target, targetNode));
                }
            }
        }

        List<Instance> instances = new ArrayList<>();
        while (queue.hasNext()) {
            WorkItem item = queue.poll();
            Instance instance = new Instance(pattern, new HashSet<>());
            extendInstance(instance, item.target, (EGroumActionNode) item.targetNode, item.pattern, (EGroumActionNode) item.patternNode, queue);
            instances.add(instance);
        }

        return instances;
    }

    private static void extendInstance(Instance instance, AUG target, EGroumActionNode targetNode, AUG pattern, EGroumActionNode patternNode, WorkQueue queue) {
        instance.addVertex(patternNode);

        EGroumDataNode patternReceiver = pattern.getReceiver(patternNode);
        if (patternReceiver != null) {
            EGroumDataNode targetReceiver = target.getReceiver(targetNode);
            if (targetReceiver.getLabel().equals(patternReceiver.getLabel())) {
                instance.addVertex(patternReceiver);
                instance.addEdge(patternReceiver, patternNode, pattern.getEdge(patternReceiver, patternNode));
                extendInstance(instance, target, targetReceiver, pattern, patternReceiver, queue);
            }
        }

        Set<EGroumActionNode> patternConditions = pattern.getConditions(patternNode);
        Set<EGroumActionNode> targetConditions = target.getConditions(targetNode);
        for (EGroumActionNode patternCondition : patternConditions) {
            for (EGroumActionNode targetCondition : targetConditions) {
                if (patternCondition.getLabel().equals(targetCondition.getLabel())) {
                    instance.addVertex(patternCondition);
                    instance.addEdge(patternCondition, patternNode, pattern.getEdge(patternCondition, patternNode));
                    extendInstance(instance, target, targetCondition, pattern, patternCondition, queue);
                    queue.remove(patternCondition);
                }
            }
        }
    }

    private static void extendInstance(Instance instance, AUG target, EGroumDataNode targetNode, AUG pattern, EGroumDataNode patternNode, WorkQueue queue) {
        Set<EGroumActionNode> patternInvocations = pattern.getInvocations(patternNode);
        Set<EGroumActionNode> targetInvocations = target.getInvocations(targetNode);
        for (EGroumActionNode patternInvocation : patternInvocations) {
            if (!instance.containsVertex(patternInvocation)) {
                for (EGroumActionNode targetInvocation : targetInvocations) {
                    if (targetInvocation.getLabel().equals(patternInvocation.getLabel())) {
                        instance.addVertex(patternInvocation);
                        instance.addEdge(patternNode, patternInvocation, pattern.getEdge(patternNode, patternInvocation));
                        extendInstance(instance, target, targetInvocation, pattern, patternInvocation, queue);
                        queue.remove(patternInvocation);
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
