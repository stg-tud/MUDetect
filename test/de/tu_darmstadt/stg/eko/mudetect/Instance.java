package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import de.tu_darmstadt.stg.eko.mudetect.model.Condition;
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
        WorkQueue nodesToCover = getCommonNodesToCover(target, pattern);
        List<Instance> instances = new ArrayList<>();
        while (nodesToCover.hasNext()) {
            WorkItem item = nodesToCover.poll();
            Instance instance = new Instance(pattern, target);
            instance.extend(item.targetNode, item.patternNode);
            instances.add(instance);
            nodesToCover.removeAll(instance.vertexSet());
        }
        return instances;
    }

    private static WorkQueue getCommonNodesToCover(AUG target, AUG pattern) {
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

        Set<Condition> patternConditions = pattern.getConditions(patternNode);
        Set<Condition> targetConditions = target.getConditions(targetNode);
        for (Condition patternCondition : patternConditions) {
            for (Condition targetCondition : targetConditions) {
                if (targetCondition.isInstanceOf(patternCondition)) {
                    EGroumActionNode patternConditionNode = patternCondition.getNode();
                    addVertex(patternConditionNode);
                    addEdge(patternConditionNode, patternNode, pattern.getEdge(patternConditionNode, patternNode));
                    extend(targetCondition.getNode(), patternConditionNode);
                }
            }
        }

        Set<EGroumNode> patternArguments = pattern.getArguments(patternNode);
        Set<EGroumNode> targetArguments = target.getArguments(targetNode);
        for (EGroumNode patternArgument : patternArguments) {
            for (EGroumNode targetArgument : targetArguments) {
                if (targetArgument.getLabel().equals(patternArgument.getLabel())) {
                    addVertex(patternArgument);
                    addEdge(patternArgument, patternNode, pattern.getEdge(patternArgument, patternNode));
                    if (patternArgument instanceof EGroumDataNode) {
                        extend((EGroumDataNode) targetArgument, (EGroumDataNode) patternArgument);
                    } else {
                        extend((EGroumActionNode) targetArgument, (EGroumActionNode) patternArgument);
                    }
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

}
