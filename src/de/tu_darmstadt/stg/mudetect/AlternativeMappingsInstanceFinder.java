package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            int nodeIndex = fragment.getPatternNodeIndex(patternNode);
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
            return fragment.getPatternNodeIndex(patternNode) == -1 && !targetNodes.contains(targetNode);
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
        private final Set<Alternative> alternatives;

        private final List<EGroumNode> exploredPatternNodes = new ArrayList<>();
        private final List<EGroumEdge> exploredPatternEdges = new ArrayList<>();

        private PatternFragment(AUG target, Pattern pattern, EGroumNode firstPatternNode) {
            this.target = target;
            this.pattern = pattern;

            this.alternatives = target.getMeaningfulActionNodes().stream()
                    .filter(targetNode -> match(firstPatternNode, targetNode))
                    .map(targetNode -> new Alternative(this, targetNode)).collect(Collectors.toSet());

            this.exploredPatternNodes.add(firstPatternNode);
        }

        int getPatternNodeIndex(EGroumNode patternNode) {
            return exploredPatternNodes.indexOf(patternNode);
        }

        Collection<Instance> findInstances() {
            if (alternatives.isEmpty()) {
                return Collections.emptySet();
            }

            Set<EGroumEdge> patternExtensionEdges = new HashSet<>(pattern.edgesOf(exploredPatternNodes.get(0)));
            while (!patternExtensionEdges.isEmpty()) {
                EGroumEdge patternEdge = patternExtensionEdges.iterator().next();

                if (!exploredPatternNodes.contains(patternEdge.getSource()))
                    exploredPatternNodes.add(patternEdge.getSource());
                if (!exploredPatternNodes.contains(patternEdge.getTarget()))
                    exploredPatternNodes.add(patternEdge.getTarget());

                exploredPatternEdges.add(patternEdge);

                patternExtensionEdges.addAll(pattern.edgesOf(patternEdge.getSource()));
                patternExtensionEdges.addAll(pattern.edgesOf(patternEdge.getTarget()));
                patternExtensionEdges.removeAll(exploredPatternEdges);

                Set<Alternative> newAlternatives = new HashSet<>();
                for (Alternative alternative : alternatives) {
                    Set<EGroumEdge> candidateTargetEdges = getCandidateTargetEdges(alternative, patternEdge);
                    boolean extended = false;
                    for (EGroumEdge targetEdge : candidateTargetEdges) {
                        newAlternatives.add(alternative.createExtension(this, targetEdge));
                        extended = true;
                    }
                    if (!extended) {
                        newAlternatives.add(alternative);
                    }
                }
                if (!newAlternatives.isEmpty()) {
                    alternatives.clear();
                    alternatives.addAll(newAlternatives);
                }
            }

            return getInstances();
        }

        private Set<EGroumEdge> getCandidateTargetEdges(Alternative alternative, EGroumEdge patternEdge) {
            EGroumNode targetSourceNode = alternative.getMappedTargetNode(patternEdge.getSource());
            EGroumNode targetTargetNode = alternative.getMappedTargetNode(patternEdge.getTarget());

            Stream<EGroumEdge> candidates;
            if (targetSourceNode != null) {
                candidates = target.outgoingEdgesOf(targetSourceNode).stream();
                if (targetTargetNode != null) {
                    candidates = candidates.filter(edge -> edge.getTarget() == targetTargetNode);
                }
            } else if (targetTargetNode != null) {
                candidates = target.incomingEdgesOf(targetTargetNode).stream();
            } else {
                // TODO I'm not sure what to do when extending by an edge whose source and target are not part of this
                // alternative. For now I say there's no candidate edge to extend this alternative with.
                candidates = Stream.empty();
            }

            return candidates
                    .filter(alternative::isUnmappedTargetEdge)
                    .filter(targetEdge -> match(patternEdge, targetEdge))
                    .collect(Collectors.toSet());
        }

        private static boolean match(EGroumEdge patternEdge, EGroumEdge targetEdge) {
            return match(patternEdge.getSource(), targetEdge.getSource())
                    && patternEdge.getLabel().equals(targetEdge.getLabel())
                    && match(patternEdge.getTarget(), targetEdge.getTarget());
        }

        private static boolean match(EGroumNode patternNode, EGroumNode targetNode) {
            return patternNode.getLabel().equals(targetNode.getLabel());
        }

        private Collection<Instance> getInstances() {
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
            Collection<Instance> newInstances = fragment.findInstances();
            instances.addAll(newInstances);
            coveredTargetNodes.addAll(fragment.getMappedTargetNodes());
        }

        removeSubInstances(instances);
        return instances;
    }


    private Collection<PatternFragment> getSingleNodeFragments(AUG target, Pattern pattern) {
        return pattern.getMeaningfulActionNodes().stream()
                .map(patternNode -> new PatternFragment(target, pattern, patternNode)).collect(Collectors.toSet());
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
