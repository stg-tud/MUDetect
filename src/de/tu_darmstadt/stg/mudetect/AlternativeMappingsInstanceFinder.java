package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.*;
import java.util.stream.Collectors;

public class AlternativeMappingsInstanceFinder implements InstanceFinder {

    private static class Alternative {
        final PatternFragment fragment;
        final List<EGroumNode> targetNodes = new ArrayList<>();
        final List<EGroumEdge> targetEdges = new ArrayList<>();

        Alternative(PatternFragment fragment, EGroumNode firstTargetNode) {
            this.fragment = fragment;
            targetNodes.add(firstTargetNode);
        }

        private Alternative(PatternFragment fragment) {
            this.fragment = fragment;
        }

        Collection<EGroumNode> getMappedTargetNodes() {
            return targetNodes;
        }

        EGroumNode getMappedTargetNode(EGroumNode patternNode) {
            int nodeIndex = fragment.exploredPatternNodes.indexOf(patternNode);
            // TODO replace null by some null object
            return nodeIndex >= 0 && nodeIndex < targetNodes.size() ? targetNodes.get(nodeIndex) : null;
        }

        boolean isUnmappedTargetEdge(EGroumEdge targetEdge) {
            return !targetEdges.contains(targetEdge);
        }

        boolean isMapped(EGroumNode targetNode, EGroumNode patternNode) {
            return getMappedTargetNode(patternNode) == targetNode;
        }

        boolean isCompatibleMappingExtension(EGroumNode targetNode, EGroumNode patternNode) {
            return !fragment.exploredPatternNodes.contains(patternNode) && !targetNodes.contains(targetNode);
        }

        Alternative createExtension(PatternFragment extendedFragment, EGroumEdge targetEdge) {
            Alternative copy = new Alternative(extendedFragment);
            copy.targetEdges.addAll(targetEdges);
            copy.targetEdges.add(targetEdge);
            copy.targetNodes.addAll(targetNodes);
            // TODO whether we need to add nodes needs only be computed once when extending the fragment!
            if (!copy.targetNodes.contains(targetEdge.getSource())) {
                copy.targetNodes.add(targetEdge.getSource());
            }
            if (!copy.targetNodes.contains(targetEdge.getTarget())) {
                copy.targetNodes.add(targetEdge.getTarget());
            }
            return copy;
        }
    }

    private static class PatternFragment {
        private final AUG target;
        private final Pattern pattern;
        private final Set<Alternative> alternatives = new HashSet<>();

        private final EGroumNode extensionPoint;
        private final boolean isOutgoingExtensionEdge;

        private final List<EGroumNode> exploredPatternNodes = new ArrayList<>();
        private final List<EGroumEdge> exploredPatternEdges = new ArrayList<>();

        private final Set<EGroumEdge> patternExtensionEdges = new HashSet<>();

        private PatternFragment(AUG target, Pattern pattern, EGroumNode firstPatternNode) {
            this.target = target;
            this.pattern = pattern;
            this.extensionPoint = firstPatternNode;
            this.isOutgoingExtensionEdge = false;

            this.exploredPatternNodes.add(firstPatternNode);

            this.patternExtensionEdges.addAll(pattern.edgesOf(extensionPoint));
        }

        PatternFragment(PatternFragment parentFragment, EGroumEdge patternEdge) {
            this.target = parentFragment.target;
            this.pattern = parentFragment.pattern;

            Set<EGroumNode> exploredPatternNodes = new HashSet<>(parentFragment.exploredPatternNodes);
            boolean sourceExplored = exploredPatternNodes.contains(patternEdge.getSource());
            boolean targetExplored = exploredPatternNodes.contains(patternEdge.getTarget());

            if (sourceExplored) {
                extensionPoint = patternEdge.getSource();
                isOutgoingExtensionEdge = true;
            } else if (targetExplored) {
                extensionPoint = patternEdge.getTarget();
                isOutgoingExtensionEdge = false;
            } else {
                throw new IllegalArgumentException("not a valid extension edge: " + patternEdge);
            }

            this.exploredPatternNodes.addAll(parentFragment.exploredPatternNodes);
            if (!sourceExplored)
                this.exploredPatternNodes.add(patternEdge.getSource());
            if (!targetExplored)
                this.exploredPatternNodes.add(patternEdge.getTarget());

            this.exploredPatternEdges.addAll(parentFragment.exploredPatternEdges);
            this.exploredPatternEdges.add(patternEdge);

            this.patternExtensionEdges.addAll(pattern.edgesOf(patternEdge.getSource()));
            this.patternExtensionEdges.addAll(pattern.edgesOf(patternEdge.getTarget()));
            this.patternExtensionEdges.removeAll(exploredPatternEdges);
            this.patternExtensionEdges.addAll(parentFragment.patternExtensionEdges);
        }

        boolean hasExtension() {
            return hasAlternatives() && !patternExtensionEdges.isEmpty();
        }

        EGroumEdge nextPatternExtensionEdge() {
            EGroumEdge nextEdge = patternExtensionEdges.iterator().next();
            patternExtensionEdges.remove(nextEdge);
            return nextEdge;
        }

        void add(Alternative alternative) {
            alternatives.add(alternative);
        }

        boolean hasAlternatives() {
            return !alternatives.isEmpty();
        }

        Set<Alternative> getAlternatives() {
            return alternatives;
        }

        Set<EGroumEdge> getCandidateTargetEdges(Alternative alternative) {
            EGroumNode mappedTargetNode = alternative.getMappedTargetNode(extensionPoint);
            if (mappedTargetNode != null) {
                return getCandidateEdges(mappedTargetNode).stream()
                        .filter(alternative::isUnmappedTargetEdge).collect(Collectors.toSet());
            } else {
                return Collections.emptySet();
            }
        }

        private Set<EGroumEdge> getCandidateEdges(EGroumNode targetNode) {
            return isOutgoingExtensionEdge ? target.outgoingEdgesOf(targetNode) : target.incomingEdgesOf(targetNode);
        }

        public Collection<Instance> getInstances() {
            Collection<Instance> instances = new HashSet<>();
            for (Alternative alternative : alternatives) {
                Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
                for (int i = 0; i < exploredPatternNodes.size() && i < alternative.targetNodes.size(); i++) {
                    targetNodeByPatternNode.put(exploredPatternNodes.get(i), alternative.targetNodes.get(i));
                }
                Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge = new HashMap<>();
                for (int i = 0; i < exploredPatternEdges.size() && i < alternative.targetEdges.size(); i++) {
                    targetEdgeByPatternEdge.put(exploredPatternEdges.get(i), alternative.targetEdges.get(i));
                }
                instances.add(new Instance(pattern, target, targetNodeByPatternNode, targetEdgeByPatternEdge));
            }
            return instances;
        }

        void removeCoveredAlternatives(Set<EGroumNode> coveredTargetNodes) {
            Iterator<Alternative> iterator = alternatives.iterator();
            while (iterator.hasNext()) {
                Alternative alternative = iterator.next();
                if (coveredTargetNodes.containsAll(alternative.getMappedTargetNodes())) {
                    iterator.remove();
                }
            }
        }

        Set<EGroumNode> getMappedTargetNodes() {
            Set<EGroumNode> mappedTargetNodes = new HashSet<>();
            for (Alternative alternative : alternatives) {
                mappedTargetNodes.addAll(alternative.getMappedTargetNodes());
            }
            return mappedTargetNodes;
        }
    }

    @Override
    public List<Instance> findInstances(AUG target, Pattern pattern) {
        List<Instance> instances = new ArrayList<>();

        Set<EGroumNode> coveredTargetNodes = new HashSet<>();
        for (PatternFragment fragment : getSingleNodeFragments(target, pattern)) {
            // When a target node N is mapped in one of the instances found by extending from a pattern node A, then
            // either this already covers all instances mapping N (in which case we don't need to explore from a mapping
            // of N anymore) or any other instance includes at least on other target node M that is not mapped in any
            // instances found by extending from A (in which case the remaining mappings of N will be found when
            // extending from a mapping of this node M). In any case, exploring from a mapping of N is redundant.
            fragment.removeCoveredAlternatives(coveredTargetNodes);
            PatternFragment extendedFragment = extend(fragment);
            instances.addAll(extendedFragment.getInstances());
            coveredTargetNodes.addAll(extendedFragment.getMappedTargetNodes());
        }

        removeSubInstances(instances);
        return instances;
    }


    private Collection<PatternFragment> getSingleNodeFragments(AUG target, Pattern pattern) {
        Map<String, Set<EGroumNode>> patternNodesByLabel = pattern.getMeaningfulActionNodesByLabel();

        Map</* pattern node */ EGroumNode, PatternFragment> alternatives = new HashMap<>();
        for (EGroumNode targetNode : target.vertexSet()) {
            String label = targetNode.getLabel();
            if (patternNodesByLabel.containsKey(label)) {
                for (EGroumNode patternNode : patternNodesByLabel.get(label)) {
                    if (!alternatives.containsKey(patternNode)) {
                        alternatives.put(patternNode, new PatternFragment(target, pattern, patternNode));
                    }
                    alternatives.get(patternNode).add(new Alternative(alternatives.get(patternNode), targetNode));
                }
            }
        }
        return alternatives.values();
    }

    private PatternFragment extend(PatternFragment fragment) {
        while (fragment.hasExtension()) {
            EGroumEdge patternEdge = fragment.nextPatternExtensionEdge();
            PatternFragment extendedFragment = new PatternFragment(fragment, patternEdge);
            for (Alternative alternative : fragment.getAlternatives()) {
                Set<EGroumEdge> candidateTargetEdges = extendedFragment.getCandidateTargetEdges(alternative);
                boolean extended = false;
                for (EGroumEdge targetEdge : candidateTargetEdges) {
                    if (match(patternEdge, targetEdge)) {
                        extendedFragment.add(alternative.createExtension(extendedFragment, targetEdge));
                        extended = true;
                    }
                }
                if (!extended) {
                    extendedFragment.add(alternative);
                }
            }
            if (extendedFragment.hasAlternatives()) {
                fragment = extendedFragment;
            }
        }

        return fragment;
    }

    private boolean match(EGroumEdge patternEdge, EGroumEdge targetEdge) {
        return match(patternEdge.getSource(), targetEdge.getSource())
                && patternEdge.getLabel().equals(targetEdge.getLabel())
                && match(patternEdge.getTarget(), targetEdge.getTarget());
    }

    private boolean match(EGroumNode patternNode, EGroumNode targetNode) {
        return patternNode.getLabel().equals(targetNode.getLabel());
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
}
