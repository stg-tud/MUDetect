package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import egroum.EGroumActionNode;
import egroum.EGroumNode;

import java.util.*;

public class GreedyInstanceFinder implements InstanceFinder {

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

        void removeAll(Collection<EGroumNode> nodes) {
            for (EGroumNode node : nodes) {
                remove(node);
            }
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
            nodesToCover.removeAll(instance.vertexSet());
        }
        removeSubInstances(instances);
        return instances;
    }

    private WorkQueue getCommonNodesToCover(AUG target, AUG pattern) {
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
        return queue;
    }

    private Map<String, Set<EGroumActionNode>> getMeaningfulActionNodesByLabel(AUG aug) {
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

    private void removeSubInstances(List<Instance> instances) {
        for (int i = 0; i < instances.size(); i++) {
            Instance instance1 = instances.get(i);
            for (int j = i + 1; j < instances.size(); j++) {
                Instance instance2 = instances.get(j);
                if (instance1.vertexSet().containsAll(instance2.vertexSet())) {
                    instances.remove(j);
                    j--;
                } else if (instance2.vertexSet().containsAll(instance1.vertexSet())) {
                    instances.remove(i);
                    i--;
                    j--;
                }
            }
        }
    }
}
