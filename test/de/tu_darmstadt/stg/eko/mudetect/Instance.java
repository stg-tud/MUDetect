package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import egroum.EGroumActionNode;
import egroum.EGroumDataNode;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.jgrapht.graph.Subgraph;

import java.util.*;

public class Instance extends Subgraph<EGroumNode, EGroumEdge, AUG> {

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

        List<Instance> instances = new ArrayList<>();
        while (queue.hasNext()) {
            WorkItem item = queue.poll();
            Instance instance = new Instance(pattern, target);
            instance.extend(item.targetNode, item.patternNode);
            instances.add(instance);
            queue.removeAll(instance.vertexSet());
        }

        return instances;
    }

    private final AUG pattern;
    private final AUG target;

    /**
     * Use for testing only.
     */
    public Instance(AUG pattern, Set<EGroumNode> vertexSubset, Set<EGroumEdge> edgeSubset) {
        super(pattern, vertexSubset, edgeSubset);
        this.pattern = pattern;
        this.target = pattern;
    }

    private Instance(AUG pattern, AUG target) {
        super(pattern, new HashSet<>());
        this.pattern = pattern;
        this.target = target;
    }

    private void extend(EGroumActionNode targetNode, EGroumActionNode patternNode) {
        addVertex(patternNode);

        EGroumDataNode patternReceiver = pattern.getReceiver(patternNode);
        if (patternReceiver != null) {
            EGroumDataNode targetReceiver = target.getReceiver(targetNode);
            if (targetReceiver.getLabel().equals(patternReceiver.getLabel())) {
                addVertex(patternReceiver);
                addEdge(patternReceiver, patternNode, pattern.getEdge(patternReceiver, patternNode));
                extend(patternReceiver, targetReceiver);
            }
        }

        Set<EGroumActionNode> patternConditions = pattern.getConditions(patternNode);
        Set<EGroumActionNode> targetConditions = target.getConditions(targetNode);
        for (EGroumActionNode patternCondition : patternConditions) {
            for (EGroumActionNode targetCondition : targetConditions) {
                if (patternCondition.getLabel().equals(targetCondition.getLabel())) {
                    addVertex(patternCondition);
                    addEdge(patternCondition, patternNode, pattern.getEdge(patternCondition, patternNode));
                    extend(targetCondition, patternCondition);
                }
            }
        }
    }

    private void extend(EGroumDataNode targetNode, EGroumDataNode patternNode) {
        Set<EGroumActionNode> patternInvocations = pattern.getInvocations(patternNode);
        Set<EGroumActionNode> targetInvocations = target.getInvocations(targetNode);
        for (EGroumActionNode patternInvocation : patternInvocations) {
            if (!containsVertex(patternInvocation)) {
                for (EGroumActionNode targetInvocation : targetInvocations) {
                    if (targetInvocation.getLabel().equals(patternInvocation.getLabel())) {
                        addVertex(patternInvocation);
                        addEdge(patternNode, patternInvocation, pattern.getEdge(patternNode, patternInvocation));
                        extend(targetInvocation, patternInvocation);
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
