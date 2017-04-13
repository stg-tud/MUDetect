package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.dot.AUGDotExporter;
import de.tu_darmstadt.stg.mudetect.dot.AUGEdgeAttributeProvider;
import de.tu_darmstadt.stg.mudetect.dot.AUGNodeAttributeProvider;
import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.mining.Pattern;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlternativeMappingsOverlapsFinder implements OverlapsFinder {

    private static class Alternative {
        final TargetFragment fragment;
        final List<EGroumNode> patternNodes = new ArrayList<>();
        final List<EGroumEdge> patternEdges = new ArrayList<>();

        Alternative(TargetFragment fragment, EGroumNode firstPatternNode) {
            this.fragment = fragment;
            patternNodes.add(firstPatternNode);
        }

        private Alternative(TargetFragment fragment) {
            this.fragment = fragment;
        }

        Collection<EGroumNode> getMappedPatternNodes() {
            return patternNodes.stream().filter(node -> node != null).collect(Collectors.toList());
        }

        int getNodeSize() {
            return getMappedPatternNodes().size();
        }

        int getEdgeSize() {
            return (int) patternEdges.stream().filter(edge -> edge != null).count();
        }

        int getSize() {
            return getNodeSize() + getEdgeSize();
        }

        EGroumNode getMappedPatternNode(EGroumNode targetNode) {
            int nodeIndex = fragment.getTargetNodeIndex(targetNode);
            return 0 <= nodeIndex && nodeIndex < patternNodes.size() ? patternNodes.get(nodeIndex) : null;
        }

        boolean isUnmappedPatternEdge(EGroumEdge patternEdge) {
            return !patternEdges.contains(patternEdge);
        }

        boolean isCompatibleExtension(EGroumEdge targetEdge, EGroumEdge patternEdge) {
            return isCompatibleExtension(targetEdge.getSource(), patternEdge.getSource()) &&
                    isCompatibleExtension(targetEdge.getTarget(), patternEdge.getTarget());
        }

        boolean isCompatibleExtension(EGroumNode targetNode, EGroumNode patternNode) {
            EGroumNode mappedPatternNode = getMappedPatternNode(targetNode);
            return (mappedPatternNode == null && !patternNodes.contains(patternNode)) || mappedPatternNode == patternNode;
        }

        Alternative createExtension(int targetEdgeIndex, int targetSourceIndex, int targetTargetIndex, EGroumEdge patternEdge) {
            Alternative copy = new Alternative(fragment);
            copy.patternEdges.addAll(patternEdges);
            insertAt(copy.patternEdges, targetEdgeIndex, patternEdge);
            copy.patternNodes.addAll(patternNodes);
            insertAt(copy.patternNodes, targetSourceIndex, patternEdge.getSource());
            insertAt(copy.patternNodes, targetTargetIndex, patternEdge.getTarget());
            return copy;
        }

        private static <T> void insertAt(List<T> list, int index, T element) {
            if (list.size() > index) {
                list.set(index, element);
            } else {
                while (list.size() <= index) {
                    if (list.size() == index) {
                        list.add(element);
                    } else {
                        list.add(null);
                    }
                }
            }
        }

        private Overlap toOverlap(AUG target, Pattern pattern, List<EGroumNode> targetNodes, List<EGroumEdge> targetEdges) {
            Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
            for (int i = 0; i < patternNodes.size(); i++) {
                EGroumNode patternNode = patternNodes.get(i);
                if (patternNode != null) {
                    targetNodeByPatternNode.put(patternNode, targetNodes.get(i));
                }
            }
            Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge = new HashMap<>();
            for (int i = 0; i < patternEdges.size(); i++) {
                EGroumEdge patternEdge = patternEdges.get(i);
                if (patternEdge != null) {
                    targetEdgeByPatternEdge.put(patternEdge, targetEdges.get(i));
                }
            }
            return new Overlap(pattern, target, targetNodeByPatternNode, targetEdgeByPatternEdge);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Alternative that = (Alternative) o;
            return Objects.equals(patternNodes, that.patternNodes) &&
                    Objects.equals(patternEdges, that.patternEdges);
        }

        @Override
        public int hashCode() {
            return Objects.hash(patternNodes, patternEdges);
        }

        @Override
        public String toString() {
            return "Alternative{" +
                    "patternNodes=" + patternNodes +
                    ", patternEdges=" + patternEdges +
                    '}';
        }
    }

    private static class ExtensionStrategy {
        private final BiPredicate<EGroumEdge, EGroumEdge> edgeMatcher;

        private final TargetFragment fragment;
        private final Set<EGroumEdge> candidates = new HashSet<>();
        private final List<EGroumEdge> exploredTargetEdges = new ArrayList<>();

        private EGroumEdge nextExtension;
        private int nextExtensionIndex;
        private Map<Alternative, Set<EGroumEdge>> nextExtensionMappingAlternatives;

        ExtensionStrategy(TargetFragment fragment, BiPredicate<EGroumEdge, EGroumEdge> edgeMatcher) {
            this.fragment = fragment;
            this.edgeMatcher = edgeMatcher;
        }

        void addExtensionCandidates(EGroumNode targetNode) {
            candidates.addAll(fragment.target.edgesOf(targetNode));
            candidates.removeAll(exploredTargetEdges);
        }

        boolean hasMoreExtensionEdges(Set<Alternative> alternatives) {
            nextExtensionMappingAlternatives = new HashMap<>();
            nextExtension = null;
            int minNumberOfAlternatives = Integer.MAX_VALUE;
            for (Iterator<EGroumEdge> edgeIt = candidates.iterator(); edgeIt.hasNext();) {
                EGroumEdge targetExtensionEdge = edgeIt.next();
                Map<Alternative, Set<EGroumEdge>> patternExtensionCandidates = new HashMap<>();
                int numberOfAlternatives = 0;
                for (Alternative alternative : alternatives) {
                    Set<EGroumEdge> mappingAlternatives = getCandidatePatternEdges(alternative, targetExtensionEdge);
                    patternExtensionCandidates.put(alternative, mappingAlternatives);
                    numberOfAlternatives += mappingAlternatives.size();
                }
                numberOfAlternatives *= getEquivalentTargetEdgeCount(targetExtensionEdge, edgeMatcher);
                if (numberOfAlternatives == 0) {
                    edgeIt.remove();
                } else if (numberOfAlternatives < minNumberOfAlternatives) {
                    nextExtension = targetExtensionEdge;
                    nextExtensionMappingAlternatives = patternExtensionCandidates;
                    minNumberOfAlternatives = numberOfAlternatives;
                }
            }
            candidates.remove(nextExtension);
            nextExtensionIndex = exploredTargetEdges.size();
            exploredTargetEdges.add(nextExtension);
            return nextExtension != null;
        }

        private Set<EGroumEdge> getCandidatePatternEdges(Alternative alternative, EGroumEdge targetEdge) {
            EGroumNode patternSourceNode = alternative.getMappedPatternNode(targetEdge.getSource());
            EGroumNode patternTargetNode = alternative.getMappedPatternNode(targetEdge.getTarget());

            Stream<EGroumEdge> candidates;
            if (patternSourceNode != null) {
                candidates = fragment.pattern.outgoingEdgesOf(patternSourceNode).stream();
                if (patternTargetNode != null) {
                    candidates = candidates.filter(edge -> edge.getTarget() == patternTargetNode);
                }
            } else if (patternTargetNode != null) {
                candidates = fragment.pattern.incomingEdgesOf(patternTargetNode).stream();
            } else {
                throw new IllegalArgumentException("cannot extend with an edge that is detached from the alternative");
            }

            return candidates
                    .filter(alternative::isUnmappedPatternEdge)
                    .filter(patternEdge -> edgeMatcher.test(targetEdge, patternEdge))
                    .filter(patternEdge -> alternative.isCompatibleExtension(targetEdge, patternEdge))
                    .collect(Collectors.toSet());
        }

        private int getEquivalentTargetEdgeCount(EGroumEdge targetEdge, BiPredicate<EGroumEdge, EGroumEdge> edgeMatcher) {
            boolean sourceNodeIsMapped = fragment.getTargetNodeIndex(targetEdge.getSource()) > -1;
            boolean targetNodeIsMapped = fragment.getTargetNodeIndex(targetEdge.getTarget()) > -1;

            Stream<EGroumEdge> candidates;
            if (sourceNodeIsMapped) {
                if (targetNodeIsMapped) {
                    candidates = Stream.of(targetEdge);
                } else {
                    candidates = fragment.target.outgoingEdgesOf(targetEdge.getSource()).stream();
                }
            } else if (targetNodeIsMapped) {
                candidates = fragment.target.incomingEdgesOf(targetEdge.getTarget()).stream();
            } else {
                throw new IllegalArgumentException("cannot extend with an edge that is detachted from the fragment");
            }

            return (int) candidates.filter(edge -> edgeMatcher.test(targetEdge, edge)).count();
        }

        EGroumEdge nextExtensionEdge() {
            return nextExtension;
        }

        int getNextExtensionIndex() {
            return nextExtensionIndex;
        }

        Set<EGroumEdge> nextExtensionMappingAlternatives(Alternative alternative) {
            return nextExtensionMappingAlternatives.get(alternative);
        }
    }

    private static class TargetFragment {
        private final AUG target;
        private final Pattern pattern;

        private final List<EGroumNode> exploredTargetNodes = new ArrayList<>();
        private ExtensionStrategy extensionStrategy;

        private TargetFragment(AUG target, Pattern pattern, EGroumNode firstTargetNode) {
            this.target = target;
            this.pattern = pattern;
            this.exploredTargetNodes.add(firstTargetNode);
        }

        EGroumNode getFirstTargetNode() {
            return exploredTargetNodes.get(0);
        }

        int getTargetNodeIndex(EGroumNode targetNode) {
            return exploredTargetNodes.indexOf(targetNode);
        }

        Set<Overlap> findLargestOverlaps(int maxNumberOfAlternatives,
                                         BiPredicate<EGroumNode, EGroumNode> nodeMatcher,
                                         BiPredicate<EGroumEdge, EGroumEdge> edgeMatcher) {
            extensionStrategy = new ExtensionStrategy(this, edgeMatcher);
            extensionStrategy.addExtensionCandidates(getFirstTargetNode());
            Set<Alternative> alternatives = getAlternatives(getFirstTargetNode(), nodeMatcher);
            while (extensionStrategy.hasMoreExtensionEdges(alternatives) && alternatives.size() <= maxNumberOfAlternatives) {
                EGroumEdge targetEdge = extensionStrategy.nextExtensionEdge();
                System.out.print("  Extending along " + targetEdge + "...");

                int targetSourceIndex = getOrCreateTargetNodeIndex(targetEdge.getSource());
                int targetTargetIndex = getOrCreateTargetNodeIndex(targetEdge.getTarget());
                int targetEdgeIndex = extensionStrategy.getNextExtensionIndex();

                Set<Alternative> newAlternatives = alternatives.stream().flatMap(alternative ->
                        extensionStrategy.nextExtensionMappingAlternatives(alternative).stream()
                                .map(patternEdge -> alternative.createExtension(targetEdgeIndex, targetSourceIndex, targetTargetIndex, patternEdge)))
                        .collect(Collectors.toSet());

                if (!newAlternatives.isEmpty()) {
                    alternatives.clear();
                    alternatives.addAll(newAlternatives);
                    extensionStrategy.addExtensionCandidates(targetEdge.getSource());
                    extensionStrategy.addExtensionCandidates(targetEdge.getTarget());
                }
                System.out.println(" now " + alternatives.size() + " alternatives.");
            }

            AlternativeMappingsOverlapsFinder.numberOfExploredAlternatives += alternatives.size();

            if (alternatives.size() > maxNumberOfAlternatives) {
                alternatives.clear();
            }

            return getLargestOverlaps(alternatives, extensionStrategy.exploredTargetEdges);
        }

        private Set<Alternative> getAlternatives(EGroumNode node, BiPredicate<EGroumNode, EGroumNode> nodeMatcher) {
            return pattern.vertexSet().stream()
                    .filter(patternNode -> nodeMatcher.test(node, patternNode))
                    .map(patternNode -> new Alternative(this, patternNode)).collect(Collectors.toSet());
        }

        private int getOrCreateTargetNodeIndex(EGroumNode targetNode) {
            int targetSourceIndex = getTargetNodeIndex(targetNode);
            if (targetSourceIndex == -1) {
                targetSourceIndex = exploredTargetNodes.size();
                exploredTargetNodes.add(targetNode);
            }
            return targetSourceIndex;
        }

        private Set<Overlap> getLargestOverlaps(Set<Alternative> alternatives, List<EGroumEdge> exploredTargetEdges) {
            int maxSize = alternatives.stream().mapToInt(Alternative::getSize).max().orElse(0);
            return alternatives.stream().filter(alt -> alt.getSize() == maxSize)
                    .map(alternative -> alternative.toOverlap(target, pattern, exploredTargetNodes, exploredTargetEdges)).collect(Collectors.toSet());
        }

        @Override
        public String toString() {
            return "TargetFragment{" +
                    "firstTargetNode=" + getFirstTargetNode() +
                    '}';
        }
    }

    public static long numberOfExploredAlternatives = 0;

    private final BiPredicate<EGroumNode, EGroumNode> nodeMatcher;

    private int maxNumberOfAlternatives = 100000;

    public AlternativeMappingsOverlapsFinder(BiPredicate<EGroumNode, EGroumNode> nodeMatcher) {
        this.nodeMatcher = nodeMatcher;
    }

    public void setMaxNumberOfAlternatives(int maxNumberOfAlternatives) {
        this.maxNumberOfAlternatives = maxNumberOfAlternatives;
    }

    @Override
    public List<Overlap> findOverlaps(AUG target, Pattern pattern) {
        List<Overlap> overlaps = new ArrayList<>();
        Set<EGroumNode> coveredTargetNodes = new HashSet<>();
        Collection<TargetFragment> singleNodeFragments = getSingleNodeFragments(target, pattern);
        if (!singleNodeFragments.isEmpty()) {
            AUGDotExporter exporter = new AUGDotExporter(new AUGNodeAttributeProvider(), new AUGEdgeAttributeProvider());
            System.out.println("Target: " + exporter.toDotGraph(target));
            System.out.println("Pattern: " + exporter.toDotGraph(pattern));
        }
        for (TargetFragment fragment : singleNodeFragments) {
            // Our goal is to find for every mappable target node at least one overlap that maps the target node. Hence,
            // if we found one before, there's no need to start from this node again.
            if (coveredTargetNodes.contains(fragment.getFirstTargetNode())) continue;

            System.out.println("Exploring from " + fragment + "...");
            Set<Overlap> newOverlaps = fragment.findLargestOverlaps(maxNumberOfAlternatives, nodeMatcher, this::match);
            for (Overlap overlap : newOverlaps) {
                overlaps.add(overlap);
                coveredTargetNodes.addAll(overlap.getMappedTargetNodes());
            }
        }

        removeSubgraphs(overlaps);
        return overlaps;
    }

    private Collection<TargetFragment> getSingleNodeFragments(AUG target, Pattern pattern) {
        return target.getMeaningfulActionNodes().stream()
                .map(targetNode -> new TargetFragment(target, pattern, targetNode)).collect(Collectors.toSet());
    }

    private boolean match(EGroumEdge targetEdge, EGroumEdge patternEdge) {
        return nodeMatcher.test(targetEdge.getSource(), patternEdge.getSource())
                && targetEdge.getLabel().equals(patternEdge.getLabel())
                && nodeMatcher.test(targetEdge.getTarget(), patternEdge.getTarget());
    }

    private void removeSubgraphs(List<Overlap> overlaps) {
        for (int i = 0; i < overlaps.size(); i++) {
            Overlap overlap1 = overlaps.get(i);
            for (int j = i + 1; j < overlaps.size(); j++) {
                Overlap overlap2 = overlaps.get(j);
                if (overlap1.coversAllTargetNodesCoveredBy(overlap2)) {
                    overlaps.remove(j); // remove overlap2
                    j--;
                } else if (overlap2.coversAllTargetNodesCoveredBy(overlap1)) {
                    overlaps.remove(i); // remove overlap1
                    i--;
                    break;
                }
            }
        }
    }
}
