package de.tu_darmstadt.stg.mudetect.aug.matchers;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.*;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.DefinitionEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ParameterEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ReceiverEdge;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jgrapht.graph.AbstractBaseGraph;

import java.util.*;
import java.util.stream.Collectors;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.constantNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.literalNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.name;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.type;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.value;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.core.AllOf.allOf;

public class AUGMatchers {
    public static Matcher<APIUsageGraph> isomorphicTo(APIUsageGraph expectedGraph) {
        Set<String> expectedNodeLabels = getNodeLabels(expectedGraph);
        if (expectedNodeLabels.size() < expectedGraph.getNodeSize()) {
            throw new IllegalArgumentException("Graph-isomorphism matching does not support graphs with multiple" +
                    " equally labelled nodes.");
        }
        Matcher<Set<String>> nodesByLabelMatcher = equalTo(expectedNodeLabels);
        Matcher<Set<String>> edgesByLabelMatcher = equalTo(getEdgeLabels(expectedGraph));

        return new BaseMatcher<APIUsageGraph>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof APIUsageGraph) {
                    APIUsageGraph actualGraph = (APIUsageGraph) o;
                    return nodesByLabelMatcher.matches(getNodeLabels(actualGraph))
                            && edgesByLabelMatcher.matches(getEdgeLabels(actualGraph));
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("graph isomorphic to ");
                description.appendValue(expectedGraph);
            }
        };
    }

    private static Set<String> getEdgeLabels(APIUsageGraph graph) {
        return graph.edgeSet().stream().map(Object::toString).collect(Collectors.toSet());
    }

    private static Set<String> getNodeLabels(APIUsageGraph graph) {
        return graph.vertexSet().stream().map(Objects::toString).collect(Collectors.toSet());
    }

    public static Matcher<APIUsageGraph> hasNode(Matcher<? super Node> nodeMatcher) {
        return new AUGElementMatcher<>(AbstractBaseGraph::vertexSet, nodeMatcher);
    }

    @SafeVarargs
    public static Matcher<APIUsageGraph> hasNodes(Matcher<? super Node>... matchers) {
        List<Matcher<? super APIUsageGraph>> all = new ArrayList<>(matchers.length);

        for (Matcher<? super Node> matcher : matchers) {
            all.add(hasNode(matcher));
        }

        return allOf(all);
    }

    public static Matcher<APIUsageGraph> hasLiteralNode(String dataType, String value) {
        return hasNode(literalNodeWith(both(type(dataType)).and(name(value))));
    }

    public static Matcher<APIUsageGraph> hasConstantNode(String dataType, String name, String value) {
        return hasNode(constantNodeWith(both(type(dataType)).and(name(name)).and(value(value))));
    }

    public static Matcher<APIUsageGraph> hasReceiverEdge(Matcher<? super Node> sourceNodeMatcher,
                                                                 Matcher<? super Node> targetNodeMatcher) {
        return hasEdge(ReceiverEdge.class, sourceNodeMatcher, targetNodeMatcher);
    }

    public static Matcher<APIUsageGraph> hasParameterEdge(Matcher<? super Node> sourceNodeMatcher,
                                                                  Matcher<? super Node> targetNodeMatcher) {
        return hasEdge(ParameterEdge.class, sourceNodeMatcher, targetNodeMatcher);
    }

    public static Matcher<APIUsageGraph> hasDefinitionEdge(Matcher<? super Node> sourceNodeMatcher,
                                                                   Matcher<? super Node> targetNodeMatcher) {
        return hasEdge(DefinitionEdge.class, sourceNodeMatcher, targetNodeMatcher);
    }

    public static Matcher<APIUsageGraph> hasOrderEdge(Matcher<? super Node> sourceNodeMatcher,
                                                              Matcher<? super Node> targetNodeMatcher) {
        return hasEdge(OrderEdge.class, sourceNodeMatcher, targetNodeMatcher);
    }

    public static Matcher<APIUsageGraph> hasSelectionEdge(Matcher<? super Node> sourceMatcher,
                                                                  Matcher<? super Node> targetMatcher) {
        return hasEdge(SelectionEdge.class, sourceMatcher, targetMatcher);
    }

    public static Matcher<APIUsageGraph> hasRepeatEdge(Matcher<? super Node> sourceMatcher,
                                                               Matcher<? super Node> targetMatcher) {
        return hasEdge(RepetitionEdge.class, sourceMatcher, targetMatcher);
    }

    public static Matcher<APIUsageGraph> hasThrowEdge(Matcher<? super Node> sourceMatcher,
                                                                Matcher<? super Node> targetMatcher) {
        return hasEdge(ThrowEdge.class, sourceMatcher, targetMatcher);
    }

    public static Matcher<APIUsageGraph> hasExceptionHandlingEdge(Matcher<? super Node> sourceMatcher,
                                                                            Matcher<? super Node> targetMatcher) {
        return hasEdge(ExceptionHandlingEdge.class, sourceMatcher, targetMatcher);
    }

    public static Matcher<APIUsageGraph> hasFinallyEdge(Matcher<? super Node> sourceMatcher,
                                                                  Matcher<? super Node> targetMatcher) {
        return hasEdge(FinallyEdge.class, sourceMatcher, targetMatcher);
    }

    public static Matcher<APIUsageGraph> hasSynchronizeEdge(Matcher<? super Node> sourceMatcher,
                                                                    Matcher<? super Node> targetMatcher) {
        return hasEdge(SynchronizationEdge.class, sourceMatcher, targetMatcher);
    }

    public static Matcher<APIUsageGraph> hasContainsEdge(Matcher<? super Node> sourceMatcher,
                                                                    Matcher<? super Node> targetMatcher) {
        return hasEdge(ContainsEdge.class, sourceMatcher, targetMatcher);
    }

    public static Matcher<APIUsageGraph> hasEdge(Matcher<? super Node> sourceMatcher,
                                                         Matcher<? super Node> targetMatcher) {
        return hasEdge(Edge.class, sourceMatcher, targetMatcher);
    }

    private static Matcher<APIUsageGraph> hasEdge(Class<? extends Edge> edgeType,
                                                          Matcher<? super Node> sourceMatcher,
                                                          Matcher<? super Node> targetMatcher) {
        return hasEdge(new EdgeMatcher(sourceMatcher, edgeType, targetMatcher));
    }

    private static Matcher<APIUsageGraph> hasEdge(Matcher<? super Edge> matcher) {
        return new AUGElementMatcher<>(AbstractBaseGraph::edgeSet, matcher);
    }
}
