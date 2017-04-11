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

        private Overlap toOverlap() {
            Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
            for (int i = 0; i < fragment.exploredTargetNodes.size() && i < patternNodes.size(); i++) {
                EGroumNode patternNode = patternNodes.get(i);
                if (patternNode != null) {
                    targetNodeByPatternNode.put(patternNode, fragment.exploredTargetNodes.get(i));
                }
            }
            Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge = new HashMap<>();
            for (int i = 0; i < fragment.exploredTargetEdges.size() && i < patternEdges.size(); i++) {
                EGroumEdge patternEdge = patternEdges.get(i);
                if (patternEdge != null) {
                    targetEdgeByPatternEdge.put(patternEdge, fragment.exploredTargetEdges.get(i));
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
                    Objects.equals(patternNodes, that.patternNodes) &&
                    Objects.equals(patternEdges, that.patternEdges);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fragment, patternNodes, patternEdges);
        }

        @Override
        public String toString() {
            return "Alternative{" +
                    "patternNodes=" + patternNodes +
                    ", patternEdges=" + patternEdges +
                    '}';
        }
    }

    private static class TargetFragment {
        private final AUG target;
        private final Pattern pattern;

        private final List<EGroumNode> exploredTargetNodes = new ArrayList<>();
        private final List<EGroumEdge> exploredTargetEdges = new ArrayList<>();

        private TargetFragment(AUG target, Pattern pattern, EGroumNode firstTargetNode) {
            this.target = target;
            this.pattern = pattern;
            this.exploredTargetNodes.add(firstTargetNode);
        }

        EGroumNode getFirstTargetNode() {
            return this.exploredTargetNodes.get(0);
        }

        int getTargetNodeIndex(EGroumNode targetNode) {
            return exploredTargetNodes.indexOf(targetNode);
        }

        Set<Overlap> findLargestOverlaps(int maxNumberOfAlternatives,
                                         BiPredicate<EGroumNode, EGroumNode> nodeMatcher,
                                         BiPredicate<EGroumEdge, EGroumEdge> edgeMatcher) {
            EGroumNode targetStartNode = getFirstTargetNode();

            Set<Alternative> alternatives = pattern.vertexSet().stream()
                    .filter(patternNode -> nodeMatcher.test(targetStartNode, patternNode))
                    .map(patternNode -> new Alternative(this, patternNode)).collect(Collectors.toSet());

            Set<EGroumEdge> targetExtensionEdges = new HashSet<>(target.edgesOf(targetStartNode));
            while (!targetExtensionEdges.isEmpty() && alternatives.size() <= maxNumberOfAlternatives) {
                EGroumEdge targetEdge = pollEdgeWithLeastAlternatives(targetExtensionEdges, alternatives, edgeMatcher);
                System.out.print("  Extending along " + targetEdge + "...");

                int targetSourceIndex = getOrCreateTargetNodeIndex(targetEdge.getSource());
                int targetTargetIndex = getOrCreateTargetNodeIndex(targetEdge.getTarget());
                int targetEdgeIndex = exploredTargetEdges.size();
                exploredTargetEdges.add(targetEdge);

                Set<Alternative> newAlternatives = new HashSet<>();
                for (Alternative alternative : alternatives) {
                    Set<EGroumEdge> candidatePatternEdges = getCandidatePatternEdges(alternative, targetEdge, edgeMatcher);
                    for (EGroumEdge patternEdge : candidatePatternEdges) {
                        newAlternatives.add(alternative.createExtension(targetEdgeIndex, targetSourceIndex, targetTargetIndex, patternEdge));
                    }
                }

                if (!newAlternatives.isEmpty()) {
                    alternatives.clear();
                    alternatives.addAll(newAlternatives);
                    targetExtensionEdges.addAll(target.edgesOf(targetEdge.getSource()));
                    targetExtensionEdges.addAll(target.edgesOf(targetEdge.getTarget()));
                    targetExtensionEdges.removeAll(exploredTargetEdges);
                }
                System.out.println(" now " + alternatives.size() + " alternatives.");
            }

            AlternativeMappingsOverlapsFinder.numberOfExploredAlternatives += alternatives.size();

            if (alternatives.size() > maxNumberOfAlternatives) {
                alternatives.clear();
            }

            return getLargestOverlaps(alternatives);
        }

        private EGroumEdge pollEdgeWithLeastAlternatives(Set<EGroumEdge> targetExtensionEdges,
                                                         Set<Alternative> alternatives,
                                                         BiPredicate<EGroumEdge, EGroumEdge> edgeMatcher) {
            int minNumberOfAlternatives = Integer.MAX_VALUE;
            EGroumEdge bestTargetEdge = null;
            for (EGroumEdge targetExtensionEdge : targetExtensionEdges) {
                int numberOfAlternatives = 0;
                for (Alternative alternative : alternatives) {
                    numberOfAlternatives += getCandidatePatternEdges(alternative, targetExtensionEdge, edgeMatcher).size();
                }
                if (numberOfAlternatives < minNumberOfAlternatives) {
                    bestTargetEdge = targetExtensionEdge;
                    minNumberOfAlternatives = numberOfAlternatives;
                }
            }
            targetExtensionEdges.remove(bestTargetEdge);
            return bestTargetEdge;
        }

        private Set<EGroumEdge> getCandidatePatternEdges(Alternative alternative, EGroumEdge targetEdge, BiPredicate<EGroumEdge, EGroumEdge> edgeMatcher) {
            EGroumNode patternSourceNode = alternative.getMappedPatternNode(targetEdge.getSource());
            EGroumNode patternTargetNode = alternative.getMappedPatternNode(targetEdge.getTarget());

            Stream<EGroumEdge> candidates;
            if (patternSourceNode != null) {
                candidates = pattern.outgoingEdgesOf(patternSourceNode).stream();
                if (patternTargetNode != null) {
                    candidates = candidates.filter(edge -> edge.getTarget() == patternTargetNode);
                }
            } else if (patternTargetNode != null) {
                candidates = pattern.incomingEdgesOf(patternTargetNode).stream();
            } else {
                throw new IllegalStateException("cannot extend with an edge that is detached from the alternative");
            }

            return candidates
                    .filter(alternative::isUnmappedPatternEdge)
                    .filter(patternEdge -> edgeMatcher.test(targetEdge, patternEdge))
                    .filter(patternEdge -> alternative.isCompatibleExtension(targetEdge, patternEdge))
                    .collect(Collectors.toSet());
        }

        private int getOrCreateTargetNodeIndex(EGroumNode source) {
            int targetSourceIndex = getTargetNodeIndex(source);
            if (targetSourceIndex == -1) {
                targetSourceIndex = exploredTargetNodes.size();
                exploredTargetNodes.add(source);
            }
            return targetSourceIndex;
        }

        private Set<Overlap> getLargestOverlaps(Set<Alternative> alternatives) {
            int maxSize = alternatives.stream().mapToInt(Alternative::getSize).max().orElse(0);
            return alternatives.stream().filter(alt -> alt.getSize() == maxSize)
                    .map(Alternative::toOverlap).collect(Collectors.toSet());
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
