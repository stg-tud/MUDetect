package de.tu_darmstadt.stg.mudetect.aug.matchers;

import de.tu_darmstadt.stg.mudetect.aug.model.*;
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
    public static Matcher<? super APIUsageGraph> hasNode(Matcher<? super Node> nodeMatcher) {
        return new AUGElementMatcher<>(AbstractBaseGraph::vertexSet, nodeMatcher);
    }

    @SafeVarargs
    public static Matcher<? super APIUsageGraph> hasNodes(Matcher<? super Node>... matchers) {
        List<Matcher<? super APIUsageGraph>> all = new ArrayList<>(matchers.length);

        for (Matcher<? super Node> matcher : matchers) {
            all.add(hasNode(matcher));
        }

        return allOf(all);
    }

    public static Matcher<? super APIUsageGraph> hasReceiverEdge(Matcher<? super Node> sourceNodeMatcher,
                                                                 Matcher<? super Node> targetNodeMatcher) {
        return hasEdge(ReceiverEdge.class, sourceNodeMatcher, targetNodeMatcher);
    }

    public static Matcher<? super APIUsageGraph> hasParameterEdge(Matcher<? super Node> sourceNodeMatcher,
                                                                  Matcher<? super Node> targetNodeMatcher) {
        return hasEdge(ParameterEdge.class, sourceNodeMatcher, targetNodeMatcher);
    }

    public static Matcher<? super APIUsageGraph> hasDefinitionEdge(Matcher<? super Node> sourceNodeMatcher,
                                                                   Matcher<? super Node> targetNodeMatcher) {
        return hasEdge(DefinitionEdge.class, sourceNodeMatcher, targetNodeMatcher);
    }

    public static Matcher<? super APIUsageGraph> hasOrderEdge(Matcher<? super Node> sourceNodeMatcher,
                                                              Matcher<? super Node> targetNodeMatcher) {
        return hasEdge(OrderEdge.class, sourceNodeMatcher, targetNodeMatcher);
    }

    public static Matcher<? super APIUsageGraph> hasSelectionEdge(Matcher<? super Node> sourceMatcher,
                                                                  Matcher<? super Node> targetMatcher) {
        return hasEdge(SelectionEdge.class, sourceMatcher, targetMatcher);
    }

    public static Matcher<? super APIUsageGraph> hasRepeatEdge(Matcher<? super Node> sourceMatcher,
                                                               Matcher<? super Node> targetMatcher) {
        return hasEdge(RepetitionEdge.class, sourceMatcher, targetMatcher);
    }

    public static Matcher<? super APIUsageExample> hasThrowEdge(Matcher<? super Node> sourceMatcher,
                                                                Matcher<? super Node> targetMatcher) {
        return hasEdge(ThrowEdge.class, sourceMatcher, targetMatcher);
    }

    public static Matcher<? super APIUsageExample> hasExceptionHandlingEdge(Matcher<? super Node> sourceMatcher,
                                                                            Matcher<? super Node> targetMatcher) {
        return hasEdge(ExceptionHandlingEdge.class, sourceMatcher, targetMatcher);
    }

    public static Matcher<? super APIUsageExample> hasFinallyEdge(Matcher<? super Node> sourceMatcher,
                                                                  Matcher<? super Node> targetMatcher) {
        return hasEdge(FinallyEdge.class, sourceMatcher, targetMatcher);
    }

    public static Matcher<? super APIUsageGraph> hasSynchronizeEdge(Matcher<? super Node> sourceMatcher,
                                                                    Matcher<? super Node> targetMatcher) {
        return hasEdge(SynchronizationEdge.class, sourceMatcher, targetMatcher);
    }

    public static Matcher<? super APIUsageGraph> hasContainsEdge(Matcher<? super Node> sourceMatcher,
                                                                    Matcher<? super Node> targetMatcher) {
        return hasEdge(ContainsEdge.class, sourceMatcher, targetMatcher);
    }

    public static Matcher<? super APIUsageGraph> hasEdge(Matcher<? super Node> sourceMatcher,
                                                         Matcher<? super Node> targetMatcher) {
        return hasEdge(Edge.class, sourceMatcher, targetMatcher);
    }

    private static Matcher<? super APIUsageGraph> hasEdge(Class<? extends Edge> edgeType,
                                                          Matcher<? super Node> sourceMatcher,
                                                          Matcher<? super Node> targetMatcher) {
        return hasEdge(new EdgeMatcher(sourceMatcher, edgeType, targetMatcher));
    }

    private static Matcher<? super APIUsageGraph> hasEdge(Matcher<? super Edge> matcher) {
        return new AUGElementMatcher<>(AbstractBaseGraph::edgeSet, matcher);
    }
}
