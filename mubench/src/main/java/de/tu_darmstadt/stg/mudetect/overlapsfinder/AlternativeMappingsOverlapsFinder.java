package de.tu_darmstadt.stg.mudetect.overlapsfinder;

import de.tu_darmstadt.stg.mubench.NoEdgeOrder;
import de.tu_darmstadt.stg.mudetect.InstanceMethodCallPredicate;
import de.tu_darmstadt.stg.mudetect.OverlapsFinder;
import de.tu_darmstadt.stg.mudetect.aug.model.*;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.*;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.DefinitionEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ParameterEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ReceiverEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.AUGDotExporter;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.DisplayAUGDotExporter;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlternativeMappingsOverlapsFinder implements OverlapsFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlternativeMappingsOverlapsFinder.class);

    private final Config config;

    private static class Alternative {
        final List<Node> patternNodes = new ArrayList<>();
        final List<Edge> patternEdges = new ArrayList<>();

        Alternative(Node firstPatternNode) {
            patternNodes.add(firstPatternNode);
        }

        private Alternative() {}

        Collection<Node> getMappedPatternNodes() {
            return patternNodes.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }

        int getNodeSize() {
            return getMappedPatternNodes().size();
        }

        int getEdgeSize() {
            return (int) patternEdges.stream().filter(Objects::nonNull).count();
        }

        int getSize() {
            return getNodeSize() + getEdgeSize();
        }

        Node getMappedPatternNode(int targetNodeIndex) {
            return 0 <= targetNodeIndex && targetNodeIndex < patternNodes.size() ? patternNodes.get(targetNodeIndex) : null;
        }

        boolean isUnmappedPatternEdge(Edge patternEdge) {
            return !patternEdges.contains(patternEdge);
        }

        boolean isCompatibleExtension(int targetEdgeSourceIndex, int targetEdgeTargetIndex, Edge patternEdge) {
            return isCompatibleExtension(targetEdgeSourceIndex, patternEdge.getSource()) &&
                    isCompatibleExtension(targetEdgeTargetIndex, patternEdge.getTarget());
        }

        boolean isCompatibleExtension(int targetNodeIndex, Node patternNode) {
            Node mappedPatternNode = getMappedPatternNode(targetNodeIndex);
            return (mappedPatternNode == null && !patternNodes.contains(patternNode)) || mappedPatternNode == patternNode;
        }

        Alternative createExtension(int targetEdgeIndex, int targetSourceIndex, int targetTargetIndex, Edge patternEdge) {
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

        private Overlap toOverlap(APIUsageExample target, APIUsagePattern pattern, List<Node> targetNodes, List<Edge> targetEdges) {
            Map<Node, Node> targetNodeByPatternNode = new HashMap<>();
            for (int i = 0; i < patternNodes.size(); i++) {
                Node patternNode = patternNodes.get(i);
                if (patternNode != null) {
                    targetNodeByPatternNode.put(patternNode, targetNodes.get(i));
                }
            }
            Map<Edge, Edge> targetEdgeByPatternEdge = new HashMap<>();
            for (int i = 0; i < patternEdges.size(); i++) {
                Edge patternEdge = patternEdges.get(i);
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
        private final APIUsageExample target;
        private final APIUsagePattern pattern;
        private final Config config;

        private final Set<Edge> candidates = new LinkedHashSet<>();
        private final List<Edge> exploredTargetEdges = new ArrayList<>();
        private final List<Node> exploredTargetNodes = new ArrayList<>();
        private Edge nextExtensionEdge;
        private int nextExtensionEdgeIndex;
        private Map<Alternative, Set<Edge>> nextExtensionMappingAlternatives;

        ExtensionStrategy(APIUsageExample target, APIUsagePattern pattern, Config config) {
            this.target = target;
            this.pattern = pattern;
            this.config = config;
        }

        Set<Overlap> findLargestOverlaps(Node startTargetNode) {
            this.candidates.addAll(target.edgesOf(startTargetNode));
            this.exploredTargetNodes.add(startTargetNode);
            Set<Alternative> alternatives = getAlternatives(startTargetNode, pattern);
            while (hasMoreExtensionEdges(alternatives) && alternatives.size() <= config.maxNumberOfAlternatives) {
                Edge nextExtensionEdge = nextExtensionEdge();
                int nextExtensionEdgeIndex = getNextExtensionEdgeIndex();
                int nextExtensionEdgeSourceIndex = getOrCreateTargetNodeIndex(target.getEdgeSource(nextExtensionEdge));
                int nextExtensionEdgeTargetIndex = getOrCreateTargetNodeIndex(target.getEdgeTarget(nextExtensionEdge));
                LOGGER.debug("  Extending along {}...", nextExtensionEdge);

                // Do not inline this variable, because the Eclipse compiler doesn't manage the type inference.
                Stream<Alternative> alternativeStream = alternatives.stream().flatMap(alternative ->
                        getNextExtensionEdgeMappingAlternatives(alternative).stream()
                                .map(patternEdge -> alternative.createExtension(
                                        nextExtensionEdgeIndex,
                                        nextExtensionEdgeSourceIndex,
                                        nextExtensionEdgeTargetIndex,
                                        patternEdge)));
                Set<Alternative> newAlternatives = alternativeStream.collect(Collectors.toSet());

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
            Edge priorityEdge = tryGetCorrespondingDirectEdgeFromTarget(nextExtensionEdge);
            nextExtensionEdge = null;
            nextExtensionMappingAlternatives = new LinkedHashMap<>();
            int minNumberOfAlternatives = Integer.MAX_VALUE;
            int maxIncomingParameterEdgeSourceNodeDegree = -1;
            for (Iterator<Edge> edgeIt = candidates.iterator(); edgeIt.hasNext();) {
                Edge targetExtensionEdge = edgeIt.next();
                Map<Alternative, Set<Edge>> patternExtensionCandidates = new LinkedHashMap<>();
                int targetEdgeSourceIndex = getTargetNodeIndex(target.getEdgeSource(targetExtensionEdge));
                int targetEdgeTargetIndex = getTargetNodeIndex(target.getEdgeTarget(targetExtensionEdge));
                int numberOfAlternatives = 0;
                for (Alternative alternative : alternatives) {
                    Set<Edge> mappingAlternatives = null;
                    if (!config.extensionEdgeTypes.contains(targetExtensionEdge.getClass())) {
                        Node patternSourceNode = alternative.getMappedPatternNode(targetEdgeSourceIndex);
                        Node patternTargetNode = alternative.getMappedPatternNode(targetEdgeTargetIndex);
                        if (patternSourceNode == null || patternTargetNode == null) {
                            mappingAlternatives = new HashSet<>();
                        }
                    }
                    if (mappingAlternatives == null) {
                        mappingAlternatives = getCandidatePatternEdges(targetEdgeTargetIndex, targetExtensionEdge, targetEdgeSourceIndex, this.pattern, alternative, this::match);
                        if (config.matchEntireConditions) {
                            mappingAlternatives = filterToMatchEntireConditions(target, targetExtensionEdge, pattern, mappingAlternatives, this::match);
                        }
                    }
                    patternExtensionCandidates.put(alternative, mappingAlternatives);
                    numberOfAlternatives += mappingAlternatives.size();
                }
                if (targetExtensionEdge == priorityEdge) {
                    numberOfAlternatives *= -1;
                } else {
                    numberOfAlternatives *= getEquivalentTargetEdgeCount(this.target, targetExtensionEdge, targetEdgeSourceIndex, targetEdgeTargetIndex, this::match);
                }

                int incomingParameterEdgeSourceNodeDegree = getIncomingParameterEdgeSourceNodeDegree(targetExtensionEdge);

                if (numberOfAlternatives == 0) {
                    // TODO we currently cannot prune here, as long as we exclude OrderEdges from extension if at least
                    // one of their endpoints is not mapped. This means an edge could become mappable later.
                    //edgeIt.remove();
                } else if (numberOfAlternatives < minNumberOfAlternatives
                        || (numberOfAlternatives == minNumberOfAlternatives && config.edgeOrder.test(targetExtensionEdge, nextExtensionEdge))
                        || (numberOfAlternatives == minNumberOfAlternatives && nextExtensionEdge instanceof ParameterEdge && maxIncomingParameterEdgeSourceNodeDegree < incomingParameterEdgeSourceNodeDegree)) {
                    nextExtensionEdge = targetExtensionEdge;
                    nextExtensionMappingAlternatives = patternExtensionCandidates;
                    minNumberOfAlternatives = numberOfAlternatives;
                    maxIncomingParameterEdgeSourceNodeDegree = incomingParameterEdgeSourceNodeDegree;
                }
            }
            if (nextExtensionEdge != null) {
                candidates.remove(nextExtensionEdge);
                nextExtensionEdgeIndex = exploredTargetEdges.size();
                exploredTargetEdges.add(nextExtensionEdge);
                candidates.addAll(target.edgesOf(target.getEdgeSource(nextExtensionEdge)));
                candidates.addAll(target.edgesOf(target.getEdgeTarget(nextExtensionEdge)));
                candidates.removeAll(exploredTargetEdges);
                return true;
            } else {
                return false;
            }
        }

        /**
         * This method returns the edge degree of the edge's source node, if the edge is a parameter edge and its source
         * node is not yet mapped. See
         * ParameterMappingTest#prefersParameterWithMultipleConnectionsToReduceRiskOfUnluckyMapping for motivation.
         */
        private int getIncomingParameterEdgeSourceNodeDegree(Edge extensionEdge) {
            boolean sourceNodeIsNotMapped = getTargetNodeIndex(target.getEdgeSource(extensionEdge)) == -1;
            if (sourceNodeIsNotMapped && extensionEdge instanceof ParameterEdge) {
                return target.edgesOf(target.getEdgeSource(extensionEdge)).size();
            } else {
                return -1;
            }
        }

        private Edge tryGetCorrespondingDirectEdgeFromTarget(Edge possiblyIndirectEdge) {
            if (possiblyIndirectEdge == null) {
                return null;
            }
            Set<Node> intermediateNodes = new HashSet<>();
            Node edgeSource = target.getEdgeSource(possiblyIndirectEdge);
            Node edgeTarget = target.getEdgeTarget(possiblyIndirectEdge);
            for (Edge outEdge: target.outgoingEdgesOf(edgeSource)) {
                Node outEdgeTarget = target.getEdgeTarget(outEdge);
                if (outEdge instanceof DataFlowEdge &&
                            (!(edgeSource instanceof DataNode) || outEdgeTarget instanceof DataNode))
                        intermediateNodes.add(outEdgeTarget);
                }
                for (Edge inEdge : target.incomingEdgesOf(edgeTarget)) {
                    Node inEdgeSource = target.getEdgeSource(inEdge);
                    if (inEdge.getType() == possiblyIndirectEdge.getType() && intermediateNodes.contains(inEdgeSource)) {
                        if (candidates.contains(inEdge)) {
                            return inEdge;
                        }
                    }
                }
            return null;
        }

        private static int getEquivalentTargetEdgeCount(APIUsageExample target, Edge targetEdge, int targetEdgeSourceIndex, int targetEdgeTargetIndex, BiPredicate<Edge, Edge> edgeMatcher) {
            boolean sourceNodeIsMapped = targetEdgeSourceIndex > -1;
            boolean targetNodeIsMapped = targetEdgeTargetIndex > -1;

            Stream<Edge> candidates;
            if (sourceNodeIsMapped) {
                if (targetNodeIsMapped) {
                    candidates = Stream.of(targetEdge);
                } else {
                    candidates = target.outgoingEdgesOf(target.getEdgeSource(targetEdge)).stream();
                }
            } else if (targetNodeIsMapped) {
                candidates = target.incomingEdgesOf(target.getEdgeTarget(targetEdge)).stream();
            } else {
                throw new IllegalArgumentException("cannot extend with an edge that is detached from the fragment");
            }

            return (int) candidates.filter(otherTargetEdge -> edgeMatcher.test(targetEdge, otherTargetEdge)).count();
        }

        private static Set<Edge> getCandidatePatternEdges(int targetEdgeTargetIndex, Edge targetEdge, int targetEdgeSourceIndex, APIUsagePattern pattern, Alternative alternative, BiPredicate<Edge, Edge> edgeMatcher) {
            Node patternSourceNode = alternative.getMappedPatternNode(targetEdgeSourceIndex);
            Node patternTargetNode = alternative.getMappedPatternNode(targetEdgeTargetIndex);

            Stream<Edge> candidates;
            if (patternSourceNode != null) {
                candidates = pattern.outgoingEdgesOf(patternSourceNode).stream();
                if (patternTargetNode != null) {
                    candidates = candidates.filter(edge -> pattern.getEdgeTarget(edge) == patternTargetNode);
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

        private static Set<Edge> filterToMatchEntireConditions(APIUsageExample target, Edge targetEdge, APIUsagePattern pattern, Set<Edge> patternCandidateEdges, BiPredicate<Edge, Edge> edgeMatcher) {
            if (targetEdge instanceof ConditionEdge) {
                Set<Edge> targetParameters = target.incomingEdgesOf(target.getEdgeSource(targetEdge));
                for (Iterator<Edge> iterator = patternCandidateEdges.iterator(); iterator.hasNext(); ) {
                    Edge patternCandidateEdge = iterator.next();
                    Set<Edge> patternParameters = pattern.incomingEdgesOf(pattern.getEdgeSource(patternCandidateEdge));
                    if (!areEquivalent(targetParameters, patternParameters, edgeMatcher)) {
                        iterator.remove();
                    }
                }
            }
            return patternCandidateEdges;
        }

        private static boolean areEquivalent(Set<Edge> targetParameters, Set<Edge> patternParameters, BiPredicate<Edge, Edge> edgeMatcher) {
            if (targetParameters.size() == patternParameters.size()) {
                patternParameters = new HashSet<>(patternParameters);
                for (Edge targetParameter : targetParameters) {
                    for (Iterator<Edge> iterator = patternParameters.iterator(); iterator.hasNext(); ) {
                        Edge patternParameter = iterator.next();
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


        private int getOrCreateTargetNodeIndex(Node targetNode) {
            int targetSourceIndex = getTargetNodeIndex(targetNode);
            if (targetSourceIndex == -1) {
                targetSourceIndex = exploredTargetNodes.size();
                exploredTargetNodes.add(targetNode);
            }
            return targetSourceIndex;
        }

        private int getTargetNodeIndex(Node targetNode) {
            return exploredTargetNodes.indexOf(targetNode);
        }

        private boolean match(Edge targetEdge, Edge patternEdge) {
            return config.nodeMatcher.test(targetEdge.getSource(), patternEdge.getSource())
                    && config.edgeMatcher.test(targetEdge, patternEdge)
                    && config.nodeMatcher.test(targetEdge.getTarget(), patternEdge.getTarget());
        }

        private Set<Alternative> getAlternatives(Node targetNode, APIUsagePattern pattern) {
            return pattern.vertexSet().stream()
                    .filter(patternNode -> config.nodeMatcher.test(targetNode, patternNode))
                    .map(Alternative::new).collect(Collectors.toSet());
        }

        private Stream<Alternative> getLargestAlternatives(Set<Alternative> alternatives) {
            int maxSize = alternatives.stream().mapToInt(Alternative::getSize).max().orElse(0);
            return alternatives.stream().filter(alt -> alt.getSize() == maxSize);
        }

        private Edge nextExtensionEdge() {
            return nextExtensionEdge;
        }

        private int getNextExtensionEdgeIndex() {
            return nextExtensionEdgeIndex;
        }

        private Set<Edge> getNextExtensionEdgeMappingAlternatives(Alternative alternative) {
            return nextExtensionMappingAlternatives.get(alternative);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class Config {
        /**
         * Predicate that decides whether matching is started from a given node.
         */
        public Predicate<Node> isStartNode = new InstanceMethodCallPredicate();

        /**
         * Predicate stating whether a target node (first argument) is matched by a pattern node (second argument). The
         * predicate may be asymmetric.
         */
        public BiPredicate<Node, Node> nodeMatcher;

        /**
         * Predicate stating whether a target-edge label (first argument) is matched by a pattern-edge label (second
         * argument). The predicate may be asymmetric.
         */
        public BiPredicate<Edge, Edge> edgeMatcher;

        /**
         * A predicate expressing a (partial) order over edges. When the predicate evaluates to <code>true</code>, the
         * first edge is given priority over the second edge.
         */
        public BiPredicate<Edge, Edge> edgeOrder = new NoEdgeOrder();

        /**
         * Detection is skipped when more than the designated number of alternative mappings have been explored.
         */
        public int maxNumberOfAlternatives = 100000;

        public boolean matchEntireConditions = false;

        public Set<Class<?>> extensionEdgeTypes = new HashSet<>(Arrays.asList(
                ThrowEdge.class, ExceptionHandlingEdge.class, FinallyEdge.class,
                SynchronizationEdge.class,
                RepetitionEdge.class, SelectionEdge.class,
                ReceiverEdge.class, ParameterEdge.class, DefinitionEdge.class, ContainsEdge.class
        ));
    }

    public static long numberOfExploredAlternatives = 0;

    public AlternativeMappingsOverlapsFinder(Config config) {
        this.config = config;
    }

    @Override
    public List<Overlap> findOverlaps(APIUsageExample target, APIUsagePattern pattern) {
        List<Overlap> overlaps = new ArrayList<>();
        Set<Node> coveredTargetNodes = new HashSet<>();
        Set<Node> startTargetNodes = getStartNodes(target);
        if (!startTargetNodes.isEmpty()) {
            AUGDotExporter exporter = new DisplayAUGDotExporter();
            LOGGER.debug("Target: {}", exporter.toDotGraph(target));
            LOGGER.debug("Pattern: {}", exporter.toDotGraph(pattern));
        }
        for (Node startTargetNode : startTargetNodes) {
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

    private Set<Node> getStartNodes(APIUsageExample target) {
        return target.vertexSet().stream().filter(config.isStartNode).collect(Collectors.toSet());
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
