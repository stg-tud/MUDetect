package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.dot.AUGDotExporter;
import de.tu_darmstadt.stg.mudetect.dot.AUGEdgeAttributeProvider;
import de.tu_darmstadt.stg.mudetect.dot.AUGNodeAttributeProvider;
import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.mining.Pattern;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlternativeMappingsOverlapsFinder implements OverlapsFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlternativeMappingsOverlapsFinder.class);

    private final Config config;

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
        private final Config config;

        private final Set<EGroumEdge> candidates = new HashSet<>();
        private final List<EGroumEdge> exploredTargetEdges = new ArrayList<>();
        private final List<EGroumNode> exploredTargetNodes = new ArrayList<>();
        private EGroumEdge nextExtensionEdge;
        private int nextExtensionEdgeIndex;
        private Map<Alternative, Set<EGroumEdge>> nextExtensionMappingAlternatives;

        ExtensionStrategy(AUG target, Pattern pattern, Config config) {
            this.target = target;
            this.pattern = pattern;
            this.config = config;
        }

        Set<Overlap> findLargestOverlaps(EGroumNode startTargetNode) {
            this.candidates.addAll(target.edgesOf(startTargetNode));
            this.exploredTargetNodes.add(startTargetNode);
            Set<Alternative> alternatives = getAlternatives(startTargetNode, pattern);
            while (hasMoreExtensionEdges(alternatives) && alternatives.size() <= config.maxNumberOfAlternatives) {
                EGroumEdge nextExtensionEdge = nextExtensionEdge();
                int nextExtensionEdgeIndex = getNextExtensionEdgeIndex();
                int nextExtensionEdgeSourceIndex = getOrCreateTargetNodeIndex(nextExtensionEdge.getSource());
                int nextExtensionEdgeTargetIndex = getOrCreateTargetNodeIndex(nextExtensionEdge.getTarget());
                LOGGER.debug("  Extending along {}...", nextExtensionEdge);

                Set<Alternative> newAlternatives = alternatives.stream().flatMap(alternative ->
                        getNextExtensionEdgeMappingAlternatives(alternative).stream()
                                .map(patternEdge -> alternative.createExtension(
                                        nextExtensionEdgeIndex,
                                        nextExtensionEdgeSourceIndex,
                                        nextExtensionEdgeTargetIndex,
                                        patternEdge)))
                        .collect(Collectors.toSet());

                if (!newAlternatives.isEmpty()) {
                    alternatives.clear();
                    alternatives.addAll(newAlternatives);
                }
                LOGGER.debug("  now {} alternatives.", alternatives.size());
            }

            AlternativeMappingsOverlapsFinder.numberOfExploredAlternatives += alternatives.size();

            if (alternatives.size() > config.maxNumberOfAlternatives) {
                alternatives.clear();
            }

            return getLargestAlternatives(alternatives)
                    .map(alternative -> alternative.toOverlap(target, pattern, exploredTargetNodes, exploredTargetEdges))
                    .collect(Collectors.toSet());
        }

        boolean hasMoreExtensionEdges(Set<Alternative> alternatives) {
            nextExtensionMappingAlternatives = new HashMap<>();
            nextExtensionEdge = null;
            int minNumberOfAlternatives = Integer.MAX_VALUE;
            for (Iterator<EGroumEdge> edgeIt = candidates.iterator(); edgeIt.hasNext();) {
                EGroumEdge targetExtensionEdge = edgeIt.next();
                Map<Alternative, Set<EGroumEdge>> patternExtensionCandidates = new HashMap<>();
                int targetEdgeSourceIndex = getTargetNodeIndex(targetExtensionEdge.getSource());
                int targetEdgeTargetIndex = getTargetNodeIndex(targetExtensionEdge.getTarget());
                int numberOfAlternatives = 0;
                for (Alternative alternative : alternatives) {
                    Set<EGroumEdge> mappingAlternatives = getCandidatePatternEdges(targetEdgeTargetIndex, targetExtensionEdge, targetEdgeSourceIndex, this.pattern, alternative, this::match);
                    if (config.matchEntireConditions) {
                        mappingAlternatives = filterToMatchEntireConditions(target, targetExtensionEdge, pattern, mappingAlternatives, this::match);
                    }
                    patternExtensionCandidates.put(alternative, mappingAlternatives);
                    numberOfAlternatives += mappingAlternatives.size();
                }
                numberOfAlternatives *= getEquivalentTargetEdgeCount(this.target, targetExtensionEdge, targetEdgeSourceIndex, targetEdgeTargetIndex, this::match);
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
                candidates.addAll(target.edgesOf(nextExtensionEdge.getSource()));
                candidates.addAll(target.edgesOf(nextExtensionEdge.getTarget()));
                candidates.removeAll(exploredTargetEdges);
                return true;
            } else {
                return false;
            }
        }

        private static int getEquivalentTargetEdgeCount(AUG target, EGroumEdge targetEdge, int targetEdgeSourceIndex, int targetEdgeTargetIndex, BiPredicate<EGroumEdge, EGroumEdge> edgeMatcher) {
            boolean sourceNodeIsMapped = targetEdgeSourceIndex > -1;
            boolean targetNodeIsMapped = targetEdgeTargetIndex > -1;

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
                throw new IllegalArgumentException("cannot extend with an edge that is detached from the fragment");
            }

            return (int) candidates.filter(otherTargetEdge -> edgeMatcher.test(targetEdge, otherTargetEdge)).count();
        }

        private static Set<EGroumEdge> getCandidatePatternEdges(int targetEdgeTargetIndex, EGroumEdge targetEdge, int targetEdgeSourceIndex, Pattern pattern, Alternative alternative, BiPredicate<EGroumEdge, EGroumEdge> edgeMatcher) {
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

        private static Set<EGroumEdge> filterToMatchEntireConditions(AUG target, EGroumEdge targetEdge, Pattern pattern, Set<EGroumEdge> patternCandidateEdges, BiPredicate<EGroumEdge, EGroumEdge> edgeMatcher) {
            if (targetEdge.isCond()) {
                Set<EGroumEdge> targetParameters = target.incomingEdgesOf(targetEdge.getSource());
                for (Iterator<EGroumEdge> iterator = patternCandidateEdges.iterator(); iterator.hasNext(); ) {
                    EGroumEdge patternCandidateEdge = iterator.next();
                    Set<EGroumEdge> patternParameters = pattern.incomingEdgesOf(patternCandidateEdge.getSource());
                    if (!areEquivalent(targetParameters, patternParameters, edgeMatcher)) {
                        iterator.remove();
                    }
                }
            }
            return patternCandidateEdges;
        }

        private static boolean areEquivalent(Set<EGroumEdge> targetParameters, Set<EGroumEdge> patternParameters, BiPredicate<EGroumEdge, EGroumEdge> edgeMatcher) {
            if (targetParameters.size() == patternParameters.size()) {
                patternParameters = new HashSet<>(patternParameters);
                for (EGroumEdge targetParameter : targetParameters) {
                    for (Iterator<EGroumEdge> iterator = patternParameters.iterator(); iterator.hasNext(); ) {
                        EGroumEdge patternParameter = iterator.next();
                        if (edgeMatcher.test(targetParameter, patternParameter)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                return patternParameters.isEmpty();

            }
            return false;
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

        private boolean match(EGroumEdge targetEdge, EGroumEdge patternEdge) {
            return config.nodeMatcher.test(targetEdge.getSource(), patternEdge.getSource())
                    && config.edgeMatcher.test(targetEdge.getLabel(), patternEdge.getLabel())
                    && config.nodeMatcher.test(targetEdge.getTarget(), patternEdge.getTarget());
        }

        private Set<Alternative> getAlternatives(EGroumNode targetNode, Pattern pattern) {
            return pattern.vertexSet().stream()
                    .filter(patternNode -> config.nodeMatcher.test(targetNode, patternNode))
                    .map(Alternative::new).collect(Collectors.toSet());
        }

        private Stream<Alternative> getLargestAlternatives(Set<Alternative> alternatives) {
            int maxSize = alternatives.stream().mapToInt(Alternative::getSize).max().orElse(0);
            return alternatives.stream().filter(alt -> alt.getSize() == maxSize);
        }

        private EGroumEdge nextExtensionEdge() {
            return nextExtensionEdge;
        }

        private int getNextExtensionEdgeIndex() {
            return nextExtensionEdgeIndex;
        }

        private Set<EGroumEdge> getNextExtensionEdgeMappingAlternatives(Alternative alternative) {
            return nextExtensionMappingAlternatives.get(alternative);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class Config {
        public BiPredicate<EGroumNode, EGroumNode> nodeMatcher;
        public BiPredicate<String, String> edgeMatcher = String::equals;
        public int maxNumberOfAlternatives = 100000;

        public boolean matchEntireConditions = false;
    }

    public static long numberOfExploredAlternatives = 0;

    public AlternativeMappingsOverlapsFinder(BiPredicate<EGroumNode, EGroumNode> aNodeMatcher) {
        this(new Config() {{ nodeMatcher = aNodeMatcher; }});
    }

    public AlternativeMappingsOverlapsFinder(Config config) {
        this.config = config;
    }

    @Override
    public List<Overlap> findOverlaps(AUG target, Pattern pattern) {
        List<Overlap> overlaps = new ArrayList<>();
        Set<EGroumNode> coveredTargetNodes = new HashSet<>();
        Set<EGroumNode> startTargetNodes = target.getMeaningfulActionNodes();
        if (!startTargetNodes.isEmpty()) {
            AUGDotExporter exporter = new AUGDotExporter(
                    EGroumNode::getLabel, new AUGNodeAttributeProvider(), new AUGEdgeAttributeProvider());
            LOGGER.debug("Target: {}", exporter.toDotGraph(target));
            LOGGER.debug("Pattern: {}", exporter.toDotGraph(pattern));
        }
        for (EGroumNode startTargetNode : startTargetNodes) {
            // Our goal is to find for every mappable target node at least one overlap that maps the target node. Hence,
            // if we found one before, there's no need to start from this node again.
            if (coveredTargetNodes.contains(startTargetNode)) continue;

            LOGGER.debug("Exploring from {}...", startTargetNode);
            ExtensionStrategy extensionStrategy = new ExtensionStrategy(target, pattern, config);
            for (Overlap overlap : extensionStrategy.findLargestOverlaps(startTargetNode)) {
                overlaps.add(overlap);
                coveredTargetNodes.addAll(overlap.getMappedTargetNodes());
            }
        }
        removeSubgraphs(overlaps);
        if (!startTargetNodes.isEmpty()) {
            LOGGER.debug("Found {} overlaps.", overlaps.size());
        }
        return overlaps;
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
