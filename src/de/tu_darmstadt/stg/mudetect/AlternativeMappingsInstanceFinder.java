package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.*;
import java.util.stream.Collectors;

public class AlternativeMappingsInstanceFinder implements InstanceFinder {

    private static class Fragment {
        private final AUG target;
        private final Pattern pattern;
        private final Set<InstanceBuilder> alternatives = new HashSet<>();

        private final EGroumNode extensionPoint;
        private final boolean isOutgoingExtensionEdge;

        private final Set<EGroumNode> exploredPatternNodes = new HashSet<>();
        private final Set<EGroumEdge> exploredPatternEdges = new HashSet<>();

        private final Set<EGroumEdge> patternExtensionEdges = new HashSet<>();

        private Fragment(AUG target, Pattern pattern, EGroumNode firstPatternNode) {
            this.target = target;
            this.pattern = pattern;
            this.extensionPoint = firstPatternNode;
            this.isOutgoingExtensionEdge = false;

            this.exploredPatternNodes.add(firstPatternNode);

            this.patternExtensionEdges.addAll(pattern.edgesOf(extensionPoint));
        }

        Fragment(Fragment parentFragment, EGroumEdge patternEdge) {
            this.target = parentFragment.target;
            this.pattern = parentFragment.pattern;

            if (parentFragment.exploredPatternNodes.contains(patternEdge.getSource())) {
                extensionPoint = patternEdge.getSource();
                isOutgoingExtensionEdge = true;
            } else if (parentFragment.exploredPatternNodes.contains(patternEdge.getTarget())) {
                extensionPoint = patternEdge.getTarget();
                isOutgoingExtensionEdge = false;
            } else {
                throw new IllegalArgumentException("not a valid extension edge: " + patternEdge);
            }

            this.exploredPatternNodes.addAll(parentFragment.exploredPatternNodes);
            this.exploredPatternNodes.add(patternEdge.getSource());
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
            exploredPatternEdges.add(nextEdge);
            return nextEdge;
        }

        void add(InstanceBuilder alternative) {
            alternatives.add(alternative);
        }

        boolean hasAlternatives() {
            return !alternatives.isEmpty();
        }

        Set<InstanceBuilder> getAlternatives() {
            return alternatives;
        }

        Set<EGroumEdge> getCandidateTargetEdges(InstanceBuilder alternative) {
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
            return alternatives.stream().map(InstanceBuilder::build).collect(Collectors.toSet());
        }

        void removeCoveredAlternatives(Set<EGroumNode> coveredTargetNodes) {
            Iterator<InstanceBuilder> iterator = alternatives.iterator();
            while (iterator.hasNext()) {
                InstanceBuilder alternative = iterator.next();
                if (coveredTargetNodes.containsAll(alternative.getMappedTargetNodes())) {
                    iterator.remove();
                }
            }
        }

        Set<EGroumNode> getMappedTargetNodes() {
            Set<EGroumNode> mappedTargetNodes = new HashSet<>();
            for (InstanceBuilder alternative : alternatives) {
                mappedTargetNodes.addAll(alternative.getMappedTargetNodes());
            }
            return mappedTargetNodes;
        }
    }

    @Override
    public List<Instance> findInstances(AUG target, Pattern pattern) {
        List<Instance> instances = new ArrayList<>();

        Set<EGroumNode> coveredTargetNodes = new HashSet<>();
        for (Fragment fragment : getSingleNodeFragments(target, pattern)) {
            // When a target node N is mapped in one of the instances found by extending from a pattern node A, then
            // either this already covers all instances mapping N (in which case we don't need to explore from a mapping
            // of N anymore) or any other instance includes at least on other target node M that is not mapped in any
            // instances found by extending from A (in which case the remaining mappings of N will be found when
            // extending from a mapping of this node M). In any case, exploring from a mapping of N is redundant.
            fragment.removeCoveredAlternatives(coveredTargetNodes);
            Fragment extendedFragment = extend(fragment);
            instances.addAll(extendedFragment.getInstances());
            coveredTargetNodes.addAll(extendedFragment.getMappedTargetNodes());
        }

        removeSubInstances(instances);
        return instances;
    }


    private Collection<Fragment> getSingleNodeFragments(AUG target, Pattern pattern) {
        Map<String, Set<EGroumNode>> patternNodesByLabel = pattern.getMeaningfulActionNodesByLabel();

        Map</* pattern node */ EGroumNode, Fragment> alternatives = new HashMap<>();
        for (EGroumNode targetNode : target.vertexSet()) {
            String label = targetNode.getLabel();
            if (patternNodesByLabel.containsKey(label)) {
                for (EGroumNode patternNode : patternNodesByLabel.get(label)) {
                    if (!alternatives.containsKey(patternNode)) {
                        alternatives.put(patternNode, new Fragment(target, pattern, patternNode));
                    }
                    InstanceBuilder alternative = new InstanceBuilder(target, pattern);
                    alternative.map(targetNode, patternNode);
                    alternatives.get(patternNode).add(alternative);
                }
            }
        }
        return alternatives.values();
    }

    private Fragment extend(Fragment fragment) {
        while (fragment.hasExtension()) {
            EGroumEdge patternEdge = fragment.nextPatternExtensionEdge();
            Fragment extendedFragment = new Fragment(fragment, patternEdge);
            for (InstanceBuilder alternative : fragment.getAlternatives()) {
                Set<EGroumEdge> candidateTargetEdges = extendedFragment.getCandidateTargetEdges(alternative);
                boolean extended = false;
                for (EGroumEdge targetEdge : candidateTargetEdges) {
                    if (match(patternEdge, targetEdge)) {
                        InstanceBuilder extendedAlternative = alternative.copy();
                        extendedAlternative.map(targetEdge.getSource(), patternEdge.getSource());
                        extendedAlternative.map(targetEdge.getTarget(), patternEdge.getTarget());
                        extendedAlternative.map(targetEdge, patternEdge);
                        extendedFragment.add(extendedAlternative);
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
