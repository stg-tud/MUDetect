package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Equation;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class GreedyInstanceFinder implements InstanceFinder {

    private interface NodeMatcher {
        boolean match(EGroumNode targetNode, EGroumNode patternNode);
    }

    private static final NodeMatcher EQUAL_NODES =
            (targetNode, patternNode) -> targetNode.getLabel().equals(patternNode.getLabel());

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

    private final Predicate<Instance> instancePredicate;

    public GreedyInstanceFinder(Predicate<Instance> instancePredicate) {
        this.instancePredicate = instancePredicate;
    }

    public GreedyInstanceFinder() {
        this(i -> true);
    }

    @Override
    public List<Instance> findInstances(AUG target, Pattern pattern) {
        WorkQueue nodesToCover = getCommonNodesToCover(target, pattern);
        List<Instance> instances = new ArrayList<>();
        while (nodesToCover.hasNext()) {
            WorkItem item = nodesToCover.poll();
            final InstanceBuilder builder = new InstanceBuilder(target, pattern);
            extend(builder, item.targetNode, item.patternNode);
            final Instance instance = builder.build();
            if (instancePredicate.test(instance)) {
                instances.add(instance);
                nodesToCover.removeAll(instance.getMappedTargetNodes());
            }
        }
        removeSubInstances(instances);
        return instances;
    }

    private WorkQueue getCommonNodesToCover(AUG target, AUG pattern) {
        Map<String, Set<EGroumNode>> patternNodesByLabel = pattern.getMeaningfulActionNodesByLabel();
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
                    break;
                }
            }
        }
    }

    private void extend(InstanceBuilder builder, EGroumNode targetNode, EGroumNode patternNode) {
        tryExtend(builder, targetNode, patternNode);
    }

    private boolean tryExtend(InstanceBuilder builder, EGroumNode targetNode, EGroumNode patternNode) {
        final AUG target = builder.getTarget();
        final AUG pattern = builder.getPattern();

        if (patternNode.isInfixOperator()) {
            Equation targetEquation = Equation.from(targetNode, target);
            Equation patternEquation = Equation.from(patternNode, pattern);
            if (!targetEquation.isInstanceOf(patternEquation)) {
                return false;
            }
        }

        builder.map(targetNode, patternNode);

        Map<String, Set<EGroumEdge>> patternNodeInEdgesByType = pattern.getInEdgesByType(patternNode);
        Map<String, Set<EGroumEdge>> targetNodeInEdgesByType = target.getInEdgesByType(targetNode);
        for (String edgeType : patternNodeInEdgesByType.keySet()) {
            if (targetNodeInEdgesByType.containsKey(edgeType)) {
                Set<EGroumEdge> patternInEdges = patternNodeInEdgesByType.get(edgeType);
                Set<EGroumEdge> targetInEdges = targetNodeInEdgesByType.get(edgeType);
                switch (edgeType) {
                    default:
                        extendUpwards(builder, patternInEdges, targetInEdges, EQUAL_NODES);
                }
            }
        }

        Map<String, Set<EGroumEdge>> patternNodeOutEdgesByType = pattern.getOutEdgesByType(patternNode);
        Map<String, Set<EGroumEdge>> targetNodeOutEdgesByType = target.getOutEdgesByType(targetNode);
        for (String edgeType : patternNodeOutEdgesByType.keySet()) {
            if (targetNodeOutEdgesByType.containsKey(edgeType)) {
                Set<EGroumEdge> patternOutEdges = patternNodeOutEdgesByType.get(edgeType);
                Set<EGroumEdge> targetOutEdges = targetNodeOutEdgesByType.get(edgeType);
                switch (edgeType) {
                    default:
                        extendDownwards(builder, patternOutEdges, targetOutEdges, EQUAL_NODES);
                }
            }
        }

        return true;
    }

    private void extendUpwards(InstanceBuilder builder, Set<EGroumEdge> patternInEdges, Set<EGroumEdge> targetInEdges,
                               NodeMatcher matcher) {
        extend(builder, patternInEdges, targetInEdges, matcher, EGroumEdge::getSource);
    }

    private void extendDownwards(InstanceBuilder builder, Set<EGroumEdge> patternOutEdges,
                                 Set<EGroumEdge> targetOutEdges, NodeMatcher matcher) {
        extend(builder, patternOutEdges, targetOutEdges, matcher, EGroumEdge::getTarget);
    }

    private void extend(InstanceBuilder builder, Set<EGroumEdge> patternEdges, Set<EGroumEdge> targetEdges,
                        NodeMatcher matcher, Function<EGroumEdge, EGroumNode> extensionNodeSelector) {
        for (EGroumEdge patternEdge : patternEdges) {
            for (EGroumEdge targetEdge : targetEdges) {
                EGroumNode targetEdgeExtensionNode = extensionNodeSelector.apply(targetEdge);
                EGroumNode patternEdgeExtensionNode = extensionNodeSelector.apply(patternEdge);
                if (matcher.match(targetEdgeExtensionNode, patternEdgeExtensionNode)) {
                    if (builder.isCompatibleMappingExtension(targetEdgeExtensionNode, patternEdgeExtensionNode)) {
                        if (tryExtend(builder, targetEdgeExtensionNode, patternEdgeExtensionNode)) {
                            builder.map(targetEdge, patternEdge);
                        }
                    } else if (builder.isMapped(targetEdgeExtensionNode, patternEdgeExtensionNode)) {
                        builder.map(targetEdge, patternEdge);
                    }
                }
            }
        }
    }
}
