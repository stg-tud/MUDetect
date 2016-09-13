package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import egroum.EGroumActionNode;
import egroum.EGroumDataNode;
import egroum.EGroumNode;

import java.util.*;

public class InstanceFinder {

    private static class WorkItem {
        EGroumActionNode patternNode;
        EGroumActionNode targetNode;

        WorkItem(EGroumActionNode patternNode, EGroumActionNode targetNode) {
            this.patternNode = patternNode;
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
            EGroumNode nextKey = workItemsByPatternNode.keySet().iterator().next();
            WorkItem nextItem = workItemsByPatternNode.get(nextKey);
            workItemsByPatternNode.remove(nextKey);
            return nextItem;
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
                    queue.add(new WorkItem(patternNode, (EGroumActionNode) targetNode));
                }
            }
        }

        InstanceFinder finder = new InstanceFinder(pattern, target, queue);

        List<Instance> instances = new ArrayList<>();
        while (queue.hasNext()) {
            instances.add(finder.findInstance(queue.poll()));
        }

        return instances;
    }

    private final AUG pattern;
    private final AUG target;
    private final WorkQueue workQueue;

    private InstanceFinder(AUG pattern, AUG target, WorkQueue workQueue) {
        this.pattern = pattern;
        this.target = target;
        this.workQueue = workQueue;
    }

    private Instance findInstance(WorkItem item) {
        Instance instance = new Instance(pattern, new HashSet<>());
        extendInstance(instance, item.targetNode, item.patternNode);
        return instance;
    }

    private void extendInstance(Instance instance, EGroumActionNode targetNode, EGroumActionNode patternNode) {
        instance.addVertex(patternNode);

        EGroumDataNode patternReceiver = pattern.getReceiver(patternNode);
        if (patternReceiver != null) {
            EGroumDataNode targetReceiver = target.getReceiver(targetNode);
            if (targetReceiver.getLabel().equals(patternReceiver.getLabel())) {
                instance.addVertex(patternReceiver);
                instance.addEdge(patternReceiver, patternNode, pattern.getEdge(patternReceiver, patternNode));
                extendInstance(instance, patternReceiver, targetReceiver);
            }
        }

        Set<EGroumActionNode> patternConditions = pattern.getConditions(patternNode);
        Set<EGroumActionNode> targetConditions = target.getConditions(targetNode);
        for (EGroumActionNode patternCondition : patternConditions) {
            for (EGroumActionNode targetCondition : targetConditions) {
                if (patternCondition.getLabel().equals(targetCondition.getLabel())) {
                    instance.addVertex(patternCondition);
                    instance.addEdge(patternCondition, patternNode, pattern.getEdge(patternCondition, patternNode));
                    extendInstance(instance, targetCondition, patternCondition);
                    workQueue.remove(patternCondition);
                }
            }
        }
    }

    private void extendInstance(Instance instance, EGroumDataNode targetNode, EGroumDataNode patternNode) {
        Set<EGroumActionNode> patternInvocations = pattern.getInvocations(patternNode);
        Set<EGroumActionNode> targetInvocations = target.getInvocations(targetNode);
        for (EGroumActionNode patternInvocation : patternInvocations) {
            if (!instance.containsVertex(patternInvocation)) {
                for (EGroumActionNode targetInvocation : targetInvocations) {
                    if (targetInvocation.getLabel().equals(patternInvocation.getLabel())) {
                        instance.addVertex(patternInvocation);
                        instance.addEdge(patternNode, patternInvocation, pattern.getEdge(patternNode, patternInvocation));
                        extendInstance(instance, targetInvocation, patternInvocation);
                        workQueue.remove(patternInvocation);
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
