package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.dot.AUGDotExporter;
import de.tu_darmstadt.stg.mudetect.dot.AUGEdgeAttributeProvider;
import de.tu_darmstadt.stg.mudetect.dot.AUGNodeAttributeProvider;
import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlternativeMappingsOverlapsFinder implements OverlapsFinder {

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
            return targetNodes.stream().filter(node -> node != null).collect(Collectors.toList());
        }

        int getNodeSize() {
            return getMappedTargetNodes().size();
        }

        int getEdgeSize() {
            return (int) targetEdges.stream().filter(edge -> edge != null).count();
        }

        EGroumNode getMappedTargetNode(EGroumNode patternNode) {
            int nodeIndex = fragment.getPatternNodeIndex(patternNode);
            return 0 <= nodeIndex && nodeIndex < targetNodes.size() ? targetNodes.get(nodeIndex) : null;
        }

        boolean isUnmappedTargetEdge(EGroumEdge targetEdge) {
            return !targetEdges.contains(targetEdge);
        }

        boolean isCompatibleExtension(EGroumEdge patternEdge, EGroumEdge targetEdge) {
            return isCompatibleExtension(patternEdge.getSource(), targetEdge.getSource()) &&
                    isCompatibleExtension(patternEdge.getTarget(), targetEdge.getTarget());
        }

        boolean isCompatibleExtension(EGroumNode patternNode, EGroumNode targetNode) {
            EGroumNode mappedTargetNode = getMappedTargetNode(patternNode);
            return (mappedTargetNode == null && !targetNodes.contains(targetNode)) || mappedTargetNode == targetNode;
        }

        Alternative createExtension(int patternEdgeIndex, int patternSourceIndex, int patternTargetIndex, EGroumEdge targetEdge) {
            Alternative copy = new Alternative(fragment);
            copy.targetEdges.addAll(targetEdges);
            insertAt(copy.targetEdges, patternEdgeIndex, targetEdge);
            copy.targetNodes.addAll(targetNodes);
            insertAt(copy.targetNodes, patternSourceIndex, targetEdge.getSource());
            insertAt(copy.targetNodes, patternTargetIndex, targetEdge.getTarget());
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

        private Overlap toOverlap() {
            Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
            for (int i = 0; i < fragment.exploredPatternNodes.size() && i < targetNodes.size(); i++) {
                EGroumNode targetNode = targetNodes.get(i);
                if (targetNode != null) {
                    targetNodeByPatternNode.put(fragment.exploredPatternNodes.get(i), targetNode);
                }
            }
            Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge = new HashMap<>();
            for (int i = 0; i < fragment.exploredPatternEdges.size() && i < targetEdges.size(); i++) {
                EGroumEdge targetEdge = targetEdges.get(i);
                if (targetEdge != null) {
                    targetEdgeByPatternEdge.put(fragment.exploredPatternEdges.get(i), targetEdge);
                }
            }
            return new Overlap(fragment.pattern, fragment.target, targetNodeByPatternNode, targetEdgeByPatternEdge);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Alternative that = (Alternative) o;
            return Objects.equals(fragment, that.fragment) &&
                    Objects.equals(targetNodes, that.targetNodes) &&
                    Objects.equals(targetEdges, that.targetEdges);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fragment, targetNodes, targetEdges);
        }

        @Override
        public String toString() {
            return "Alternative{" +
                    "targetNodes=" + targetNodes +
                    ", targetEdges=" + targetEdges +
                    '}';
        }
    }

    private static class PatternFragment {
        private final AUG target;
        private final Pattern pattern;
        private final EGroumNode firstTargetNode;
        private int numberOfExploredAlternatives;

        private final List<EGroumNode> exploredPatternNodes = new ArrayList<>();
        private final List<EGroumEdge> exploredPatternEdges = new ArrayList<>();

        private PatternFragment(AUG target, Pattern pattern, EGroumNode firstPatternNode, EGroumNode firstTargetNode) {
            this.target = target;
            this.pattern = pattern;
            this.firstTargetNode = firstTargetNode;

            this.exploredPatternNodes.add(firstPatternNode);
        }

        int getPatternNodeIndex(EGroumNode patternNode) {
            return exploredPatternNodes.indexOf(patternNode);
        }

        EGroumNode getFirstTargetNode() {
            return firstTargetNode;
        }

        Overlap findLargestOverlap(int maxNumberOfAlternatives) {
            numberOfExploredAlternatives = 0;
            Set<Alternative> alternatives = new HashSet<>();
            alternatives.add(new Alternative(this, firstTargetNode));

            Set<EGroumEdge> patternExtensionEdges = new HashSet<>(pattern.edgesOf(exploredPatternNodes.get(0)));
            while (!patternExtensionEdges.isEmpty() && alternatives.size() < maxNumberOfAlternatives) {
                EGroumEdge patternEdge = pollEdgeWithLeastAlternatives(patternExtensionEdges, alternatives);
                System.out.print("  Extending along " + patternEdge + "...");

                int patternSourceIndex = getOrCreatePatternNodeIndex(patternEdge.getSource());
                int patternTargetIndex = getOrCreatePatternNodeIndex(patternEdge.getTarget());
                int patternEdgeIndex = exploredPatternEdges.size();
                exploredPatternEdges.add(patternEdge);

                Set<Alternative> newAlternatives = new HashSet<>();
                for (Alternative alternative : alternatives) {
                    Set<EGroumEdge> candidateTargetEdges = getCandidateTargetEdges(alternative, patternEdge);
                    for (EGroumEdge targetEdge : candidateTargetEdges) {
                        newAlternatives.add(alternative.createExtension(patternEdgeIndex, patternSourceIndex, patternTargetIndex, targetEdge));
                    }
                }

                if (!newAlternatives.isEmpty()) {
                    alternatives.clear();
                    alternatives.addAll(newAlternatives);
                    patternExtensionEdges.addAll(pattern.edgesOf(patternEdge.getSource()));
                    patternExtensionEdges.addAll(pattern.edgesOf(patternEdge.getTarget()));
                    patternExtensionEdges.removeAll(exploredPatternEdges);
                }
                System.out.println(" now " + alternatives.size() + " alternatives.");
            }

            numberOfExploredAlternatives = alternatives.size();

            if (alternatives.size() < maxNumberOfAlternatives)
                return getLargestAlternative(alternatives).toOverlap();
            else
                return null;
        }

        private EGroumEdge pollEdgeWithLeastAlternatives(Set<EGroumEdge> patternExtensionEdges,
                                                         Set<Alternative> alternatives) {
            int minNumberOfAlternatives = Integer.MAX_VALUE;
            EGroumEdge bestPatternEdge = null;
            for (EGroumEdge patternExtensionEdge : patternExtensionEdges) {
                int numberOfAlternatives = 0;
                for (Alternative alternative : alternatives) {
                    numberOfAlternatives += getCandidateTargetEdges(alternative, patternExtensionEdge).size();
                }
                if (numberOfAlternatives < minNumberOfAlternatives) {
                    bestPatternEdge = patternExtensionEdge;
                    minNumberOfAlternatives = numberOfAlternatives;
                }
            }
            patternExtensionEdges.remove(bestPatternEdge);
            return bestPatternEdge;
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
                throw new IllegalStateException("cannot extend with an edge that is detached from the alternative");
            }

            return candidates
                    .filter(alternative::isUnmappedTargetEdge)
                    .filter(targetEdge -> match(patternEdge, targetEdge))
                    .filter(targetEdge -> alternative.isCompatibleExtension(patternEdge, targetEdge))
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

        private int getOrCreatePatternNodeIndex(EGroumNode source) {
            int patternSourceIndex = getPatternNodeIndex(source);
            if (patternSourceIndex == -1) {
                patternSourceIndex = exploredPatternNodes.size();
                exploredPatternNodes.add(source);
            }
            return patternSourceIndex;
        }

        private Alternative getLargestAlternative(Set<Alternative> alternatives) {
            int maxSize = 0;
            Alternative candidate = null;
            for (Alternative alternative : alternatives) {
                int size = alternative.getNodeSize() + alternative.getEdgeSize();
                if (size > maxSize) {
                    maxSize = size;
                    candidate = alternative;
                }
            }
            return candidate;
        }

        int getNumberOfExploredAlternatives() {
            return numberOfExploredAlternatives;
        }

        @Override
        public String toString() {
            return "PatternFragment{" +
                    "firstTargetNode=" + firstTargetNode +
                    '}';
        }
    }

    public static long numberOfExploredAlternatives = 0;

    private final Predicate<Overlap> overlapPredicate;

    private int maxNumberOfAlternatives = 100000;

    public AlternativeMappingsOverlapsFinder(Predicate<Overlap> overlapPredicate) {
        this.overlapPredicate = overlapPredicate;
    }

    public AlternativeMappingsOverlapsFinder() {
        this(i -> true);
    }

    public void setMaxNumberOfAlternatives(int maxNumberOfAlternatives) {
        this.maxNumberOfAlternatives = maxNumberOfAlternatives;
    }

    @Override
    public List<Overlap> findOverlaps(AUG target, Pattern pattern) {
        List<Overlap> overlaps = new ArrayList<>();
        Set<EGroumNode> coveredTargetNodes = new HashSet<>();
        Collection<PatternFragment> singleNodeFragments = getSingleNodeFragments(target, pattern);
        if (!singleNodeFragments.isEmpty()) {
            AUGDotExporter exporter = new AUGDotExporter(new AUGNodeAttributeProvider(), new AUGEdgeAttributeProvider());
            System.out.println("Target: " + exporter.toDotGraph(target));
            System.out.println("Pattern: " + exporter.toDotGraph(pattern));
        }
        for (PatternFragment fragment : singleNodeFragments) {
            System.out.println("Exploring from " + fragment + "...");

            // When a target node N is mapped in one of the overlaps found by extending from a pattern node A, then
            // either this already covers all overlaps mapping N (in which case we don't need to explore from a mapping
            // of N anymore) or any other instance includes at least on other target node M that is not mapped in any
            // overlaps found by extending from A (in which case the remaining mappings of N will be found when
            // extending from a mapping of this node M). In any case, exploring from a mapping of N is redundant.
            if (coveredTargetNodes.contains(fragment.getFirstTargetNode())) continue;
            Overlap overlap = fragment.findLargestOverlap(maxNumberOfAlternatives);
            if (overlap != null && overlapPredicate.test(overlap)) {
                overlaps.add(overlap);
                coveredTargetNodes.addAll(overlap.getMappedTargetNodes());
            }
            numberOfExploredAlternatives += fragment.getNumberOfExploredAlternatives();
        }

        removeSubgraphs(overlaps);
        return overlaps;
    }

    private Collection<PatternFragment> getSingleNodeFragments(AUG target, Pattern pattern) {
        return pattern.getMeaningfulActionNodesByUniqueness().stream()
                .flatMap(patternNode -> target.getMeaningfulActionNodes().stream()
                        .filter(targetNode -> PatternFragment.match(patternNode, targetNode))
                        .map(targetNode -> new PatternFragment(target, pattern, patternNode, targetNode)))
                .collect(Collectors.toList());
    }

    private void removeSubgraphs(List<Overlap> overlaps) {
        for (int i = 0; i < overlaps.size(); i++) {
            Overlap overlap1 = overlaps.get(i);
            for (int j = i + 1; j < overlaps.size(); j++) {
                Overlap overlap2 = overlaps.get(j);
                if (overlap2.isSubgraphOf(overlap1)) {
                    overlaps.remove(j);
                    j--;
                } else if (overlap1.isSubgraphOf(overlap2)) {
                    overlaps.remove(i);
                    i--;
                    break;
                }
            }
        }
    }
}
