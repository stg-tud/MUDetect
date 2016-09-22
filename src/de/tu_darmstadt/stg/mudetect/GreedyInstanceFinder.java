package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import egroum.EGroumNode;

import java.util.*;

public class GreedyInstanceFinder implements InstanceFinder {

    private static class WorkItem {
        EGroumNode patternNode;
        EGroumNode targetNode;

        WorkItem(EGroumNode targetNode, EGroumNode patternNode) {
            this.patternNode = patternNode;
            this.targetNode = targetNode;
        }
    }

    private static class WorkQueue {
        private Map<EGroumNode, Set<WorkItem>> workItemsByTargetNode = new HashMap<>();

        void add(WorkItem item) {
            if (!workItemsByTargetNode.containsKey(item.targetNode)) {
                workItemsByTargetNode.put(item.targetNode, new HashSet<>());
            }
            workItemsByTargetNode.get(item.targetNode).add(item);
        }

        boolean hasNext() {
            return !workItemsByTargetNode.isEmpty();
        }

        WorkItem poll() {
            EGroumNode nextKey = workItemsByTargetNode.keySet().iterator().next();
            Set<WorkItem> nextItems = workItemsByTargetNode.get(nextKey);
            WorkItem nextItem = nextItems.iterator().next();
            nextItems.remove(nextItem);
            if (nextItems.isEmpty()) {
                workItemsByTargetNode.remove(nextKey);
            }
            return nextItem;
        }

        void remove(EGroumNode targetNode) {
            workItemsByTargetNode.remove(targetNode);
        }

        void removeAll(Collection<EGroumNode> targetNodes) {
            targetNodes.forEach(this::remove);
        }
    }

    @Override
    public List<Instance> findInstances(AUG target, AUG pattern) {
        WorkQueue nodesToCover = getCommonNodesToCover(target, pattern);
        List<Instance> instances = new ArrayList<>();
        while (nodesToCover.hasNext()) {
            WorkItem item = nodesToCover.poll();
            Instance instance = new Instance(pattern, target);
            instance.extend(item.targetNode, item.patternNode);
            instances.add(instance);
            nodesToCover.removeAll(instance.getMappedTargetNodes());
        }
        removeSubInstances(instances);
        return instances;
    }

    private WorkQueue getCommonNodesToCover(AUG target, AUG pattern) {
        Map<String, Set<EGroumNode>> patternNodesByLabel = getMeaningfulActionNodesByLabel(pattern);
        WorkQueue queue = new WorkQueue();
        for (EGroumNode targetNode : target.vertexSet()) {
            String label = targetNode.getLabel();
            if (patternNodesByLabel.containsKey(label)) {
                for (EGroumNode patternNode : patternNodesByLabel.get(label)) {
                    queue.add(new WorkItem(targetNode, patternNode));
                }
            }
        }
        return queue;
    }

    private Map<String, Set<EGroumNode>> getMeaningfulActionNodesByLabel(AUG aug) {
        Map<String, Set<EGroumNode>> nodesByLabel = new HashMap<>();
        for (EGroumNode node : aug.vertexSet()) {
            if (node.isMeaningfulAction()) {
                String label = node.getLabel();
                if (!nodesByLabel.containsKey(label)) {
                    nodesByLabel.put(label, new HashSet<>());
                }
                nodesByLabel.get(label).add(node);
            }
        }
        return nodesByLabel;
    }

    private void removeSubInstances(List<Instance> instances) {
        for (int i = 0; i < instances.size(); i++) {
            Instance instance1 = instances.get(i);
            for (int j = i + 1; j < instances.size(); j++) {
                Instance instance2 = instances.get(j);
                if (instance2.isSubInstanceOf(instance1)) {
                    instances.remove(j);
                    j--;
                } else if (instance1.isSubInstanceOf(instance2)) {
                    instances.remove(i);
                    i--;
                    j--;
                }
            }
        }
    }
}
