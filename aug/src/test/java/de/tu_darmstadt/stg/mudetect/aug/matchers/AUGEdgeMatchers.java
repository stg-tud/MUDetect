package de.tu_darmstadt.stg.mudetect.aug.matchers;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ReceiverEdge;
import org.hamcrest.Matcher;
import org.jgrapht.graph.AbstractBaseGraph;

public abstract class AUGEdgeMatchers {
    public static Matcher<? super APIUsageGraph> hasReceiverEdge(Matcher<? super Node> sourceNodeMatcher,
                                                                 Matcher<? super Node> targetNodeMatcher) {
        return hasEdge(sourceNodeMatcher, ReceiverEdge.class, targetNodeMatcher);
    }

    private static Matcher<? super APIUsageGraph> hasEdge(Matcher<? super Node> sourceMatcher,
                                                          Class<? extends Edge> edgeType,
                                                          Matcher<? super Node> targetMatcher) {
        return hasEdge(new EdgeMatcher(sourceMatcher, edgeType, targetMatcher));
    }

    private static Matcher<? super APIUsageGraph> hasEdge(Matcher<? super Edge> matcher) {
        return new AUGElementMatcher<>(AbstractBaseGraph::edgeSet, matcher);
    }

}
