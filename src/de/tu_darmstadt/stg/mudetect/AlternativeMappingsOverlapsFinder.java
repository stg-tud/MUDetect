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
        final List<EGroumNode> patternNodes = new ArrayList<>();
        final List<EGroumEdge> patternEdges = new ArrayList<>();

        Alternative(EGroumNode firstPatternNode) {
            patternNodes.add(firstPatternNode);
        }

        private Alternative() {}

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

        EGroumNode getMappedPatternNode(int targetNodeIndex) {
            return 0 <= targetNodeIndex && targetNodeIndex < patternNodes.size() ? patternNodes.get(targetNodeIndex) : null;
        }

        boolean isUnmappedPatternEdge(EGroumEdge patternEdge) {
            return !patternEdges.contains(patternEdge);
        }

        boolean isCompatibleExtension(int targetEdgeSourceIndex, int targetEdgeTargetIndex, EGroumEdge patternEdge) {
            return isCompatibleExtension(targetEdgeSourceIndex, patternEdge.getSource()) &&
                    isCompatibleExtension(targetEdgeTargetIndex, patternEdge.getTarget());
        }

        boolean isCompatibleExtension(int targetNodeIndex, EGroumNode patternNode) {
            EGroumNode mappedPatternNode = getMappedPatternNode(targetNodeIndex);
            return (mappedPatternNode == null && !patternNodes.contains(patternNode)) || mappedPatternNode == patternNode;
        }

        Alternative createExtension(int targetEdgeIndex, int targetSourceIndex, int targetTargetIndex, EGroumEdge patternEdge) {
            Alternative copy = new Alternative();
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
        private final AUG target;
        private final Pattern pattern;
        private final BiPredicate<EGroumEdge, EGroumEdge> edgeMatcher;

        private final Set<EGroumEdge> candidates = new HashSet<>();
        private final List<EGroumEdge> exploredTargetEdges = new ArrayList<>();
        private final List<EGroumNode> exploredTargetNodes = new ArrayList<>();
        private EGroumEdge nextExtensionEdge;
        private int nextExtensionEdgeIndex;
        private Map<Alternative, Set<EGroumEdge>> nextExtensionMappingAlternatives;
        private int nextExtensionEdgeTargetIndex;
        private int nextExtensionEdgeSourceIndex;

        ExtensionStrategy(AUG target, EGroumNode startTargetNode, Pattern pattern, BiPredicate<EGroumEdge, EGroumEdge> edgeMatcher) {
            this.target = target;
            this.pattern = pattern;
            this.edgeMatcher = edgeMatcher;
            this.candidates.addAll(target.edgesOf(startTargetNode));
            this.exploredTargetNodes.add(startTargetNode);
        }

        private int getOrCreateTargetNodeIndex(EGroumNode targetNode) {
            int targetSourceIndex = getTargetNodeIndex(targetNode);
            if (targetSourceIndex == -1) {
                targetSourceIndex = exploredTargetNodes.size();
                exploredTargetNodes.add(targetNode);
            }
            return targetSourceIndex;
        }

        private int getTargetNodeIndex(EGroumNode targetNode) {
            return exploredTargetNodes.indexOf(targetNode);
        }

        boolean hasMoreExtensionEdges(Set<Alternative> alternatives) {
            nextExtensionMappingAlternatives = new HashMap<>();
            nextExtensionEdge = null;
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
                    nextExtensionEdge = targetExtensionEdge;
                    nextExtensionMappingAlternatives = patternExtensionCandidates;
                    minNumberOfAlternatives = numberOfAlternatives;
                }
            }
            if (nextExtensionEdge != null) {
                candidates.remove(nextExtensionEdge);
                nextExtensionEdgeIndex = exploredTargetEdges.size();
                exploredTargetEdges.add(nextExtensionEdge);
                nextExtensionEdgeSourceIndex = getOrCreateTargetNodeIndex(nextExtensionEdge.getSource());
                nextExtensionEdgeTargetIndex = getOrCreateTargetNodeIndex(nextExtensionEdge.getTarget());
                candidates.addAll(target.edgesOf(nextExtensionEdge.getSource()));
                candidates.addAll(target.edgesOf(nextExtensionEdge.getTarget()));
                candidates.removeAll(exploredTargetEdges);
                return true;
            } else {
                return false;
            }
        }

        private Set<EGroumEdge> getCandidatePatternEdges(Alternative alternative, EGroumEdge targetEdge) {
            int targetEdgeSourceIndex = getTargetNodeIndex(targetEdge.getSource());
            int targetEdgeTargetIndex = getTargetNodeIndex(targetEdge.getTarget());
            EGroumNode patternSourceNode = alternative.getMappedPatternNode(targetEdgeSourceIndex);
            EGroumNode patternTargetNode = alternative.getMappedPatternNode(targetEdgeTargetIndex);

            Stream<EGroumEdge> candidates;
            if (patternSourceNode != null) {
                candidates = pattern.outgoingEdgesOf(patternSourceNode).stream();
                if (patternTargetNode != null) {
                    candidates = candidates.filter(edge -> edge.getTarget() == patternTargetNode);
                }
            } else if (patternTargetNode != null) {
                candidates = pattern.incomingEdgesOf(patternTargetNode).stream();
            } else {
                throw new IllegalArgumentException("cannot extend with an edge that is detached from the alternative");
            }

            return candidates
                    .filter(alternative::isUnmappedPatternEdge)
                    .filter(patternEdge -> edgeMatcher.test(targetEdge, patternEdge))
                    .filter(patternEdge -> alternative.isCompatibleExtension(targetEdgeSourceIndex, targetEdgeTargetIndex, patternEdge))
                    .collect(Collectors.toSet());
        }

        private int getEquivalentTargetEdgeCount(EGroumEdge targetEdge, BiPredicate<EGroumEdge, EGroumEdge> edgeMatcher) {
            boolean sourceNodeIsMapped = getTargetNodeIndex(targetEdge.getSource()) > -1;
            boolean targetNodeIsMapped = getTargetNodeIndex(targetEdge.getTarget()) > -1;

            Stream<EGroumEdge> candidates;
            if (sourceNodeIsMapped) {
                if (targetNodeIsMapped) {
                    candidates = Stream.of(targetEdge);
                } else {
                    candidates = target.outgoingEdgesOf(targetEdge.getSource()).stream();
                }
            } else if (targetNodeIsMapped) {
                candidates = target.incomingEdgesOf(targetEdge.getTarget()).stream();
            } else {
                throw new IllegalArgumentException("cannot extend with an edge that is detachted from the fragment");
            }

            return (int) candidates.filter(edge -> edgeMatcher.test(targetEdge, edge)).count();
        }

        EGroumEdge nextExtensionEdge() {
            return nextExtensionEdge;
        }

        int getNextExtensionEdgeIndex() {
            return nextExtensionEdgeIndex;
        }

        int getNextExtensionEdgeSourceIndex() {
            return nextExtensionEdgeSourceIndex;
        }

        int getNextExtensionEdgeTargetIndex() {
            return nextExtensionEdgeTargetIndex;
        }

        Set<EGroumEdge> getNextExtensionEdgeMappingAlternatives(Alternative alternative) {
            return nextExtensionMappingAlternatives.get(alternative);
        }
    }

    public static long numberOfExploredAlternatives = 0;

    private int maxNumberOfAlternatives = 100000;
    private final BiPredicate<EGroumNode, EGroumNode> nodeMatcher;

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
        Collection<EGroumNode> singleNodeFragments = target.getMeaningfulActionNodes();
        if (!singleNodeFragments.isEmpty()) {
            AUGDotExporter exporter = new AUGDotExporter(new AUGNodeAttributeProvider(), new AUGEdgeAttributeProvider());
            System.out.println("Target: " + exporter.toDotGraph(target));
            System.out.println("Pattern: " + exporter.toDotGraph(pattern));
        }
        for (EGroumNode targetNode : singleNodeFragments) {
            // Our goal is to find for every mappable target node at least one overlap that maps the target node. Hence,
            // if we found one before, there's no need to start from this node again.
            if (coveredTargetNodes.contains(targetNode)) continue;

            System.out.println("Exploring from " + targetNode + "...");
            Set<Overlap> newOverlaps = findLargestOverlaps(target, targetNode, pattern);
            for (Overlap overlap : newOverlaps) {
                overlaps.add(overlap);
                coveredTargetNodes.addAll(overlap.getMappedTargetNodes());
            }
        }

        removeSubgraphs(overlaps);
        return overlaps;
    }

    private Set<Overlap> findLargestOverlaps(AUG target, EGroumNode startTargetNode, Pattern pattern) {
        ExtensionStrategy extensionStrategy = new ExtensionStrategy(target, startTargetNode, pattern, this::match);

        Set<Alternative> alternatives = getAlternatives(startTargetNode, pattern);
        while (extensionStrategy.hasMoreExtensionEdges(alternatives) && alternatives.size() <= maxNumberOfAlternatives) {
            EGroumEdge targetEdge = extensionStrategy.nextExtensionEdge();
            System.out.print("  Extending along " + targetEdge + "...");

            Set<Alternative> newAlternatives = alternatives.stream().flatMap(alternative ->
                    extensionStrategy.getNextExtensionEdgeMappingAlternatives(alternative).stream()
                            .map(patternEdge -> alternative.createExtension(
                                    extensionStrategy.getNextExtensionEdgeIndex(),
                                    extensionStrategy.getNextExtensionEdgeSourceIndex(),
                                    extensionStrategy.getNextExtensionEdgeTargetIndex(),
                                    patternEdge)))
                    .collect(Collectors.toSet());

            if (!newAlternatives.isEmpty()) {
                alternatives.clear();
                alternatives.addAll(newAlternatives);
            }
            System.out.println(" now " + alternatives.size() + " alternatives.");
        }

        AlternativeMappingsOverlapsFinder.numberOfExploredAlternatives += alternatives.size();

        if (alternatives.size() > maxNumberOfAlternatives) {
            alternatives.clear();
        }

        return getLargestAlternatives(alternatives)
                .map(alternative -> alternative.toOverlap(
                        target, pattern,
                        extensionStrategy.exploredTargetNodes,
                        extensionStrategy.exploredTargetEdges)).collect(Collectors.toSet());
    }

    private Set<Alternative> getAlternatives(EGroumNode targetNode, Pattern pattern) {
        return pattern.vertexSet().stream()
                .filter(patternNode -> nodeMatcher.test(targetNode, patternNode))
                .map(Alternative::new).collect(Collectors.toSet());
    }

    private Stream<Alternative> getLargestAlternatives(Set<Alternative> alternatives) {
        int maxSize = alternatives.stream().mapToInt(Alternative::getSize).max().orElse(0);
        return alternatives.stream().filter(alt -> alt.getSize() == maxSize);
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
