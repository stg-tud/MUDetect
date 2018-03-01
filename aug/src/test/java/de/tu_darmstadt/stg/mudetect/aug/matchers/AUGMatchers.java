package de.tu_darmstadt.stg.mudetect.aug.matchers;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.*;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.DefinitionEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ParameterEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ReceiverEdge;
import org.hamcrest.Matcher;
import org.jgrapht.graph.AbstractBaseGraph;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.AllOf.allOf;

public class AUGMatchers {
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
