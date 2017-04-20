package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.dot.AUGDotExporter;
import de.tu_darmstadt.stg.mudetect.dot.AUGEdgeAttributeProvider;
import de.tu_darmstadt.stg.mudetect.dot.AUGNodeAttributeProvider;
import egroum.*;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jgrapht.graph.AbstractBaseGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.core.AllOf.allOf;

public class AUGTestUtils {
    public static Matcher<AUG> isEqual(AUG expected) {
        Set<String> expectedNodeLabels = getNodeLabels(expected);
        Set<String> expectedEdgeLabels = getEdgeLabels(expected);

        return new BaseMatcher<AUG>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof AUG) {
                    AUG actual = (AUG) item;
                    return getNodeLabels(actual).equals(expectedNodeLabels) &&
                            getEdgeLabels(actual).equals(expectedEdgeLabels);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(expected);
            }
        };
    }

    private static Set<String> getNodeLabels(AUG expected) {
        Set<String> expectedNodeLabels = expected.vertexSet().stream()
                .map(EGroumNode::getLabel).collect(Collectors.toSet());
        if (expectedNodeLabels.size() < expected.getNodeSize()) {
            throw new IllegalArgumentException("cannot handle AUG with multiple equally-labelled nodes");
        }
        return expectedNodeLabels;
    }

    private static Set<String> getEdgeLabels(AUG aug) {
        Set<String> expectedEdgeLabels = aug.edgeSet().stream()
                .map(AUGTestUtils::getEdgeLabel).collect(Collectors.toSet());
        if (expectedEdgeLabels.size() < aug.getEdgeSize()) {
            throw new IllegalArgumentException("cannot handle AUG with multiple equally-labelled edges between the same nodes");
        }
        return expectedEdgeLabels;
    }

    private static String getEdgeLabel(EGroumEdge edge) {
        return edge.getSource().getLabel() + "--(" + edge.getLabel() + ")-->" + edge.getTarget().getLabel();
    }

    public static Matcher<? super AUG> hasNode(Matcher<? super EGroumNode> matcher) {
        return new AUGElementMatcher<>(AbstractBaseGraph::vertexSet, matcher);
    }

    @SafeVarargs
    public static Matcher<? super AUG> hasNodes(Matcher<? super EGroumNode>... matchers) {
        List<Matcher<? super AUG>> all = new ArrayList<>(matchers.length);

        for (Matcher<? super EGroumNode> matcher : matchers) {
            all.add(hasNode(matcher));
        }

        return allOf(all);
    }

    public static Matcher<? super EGroumNode> actionNodeWithLabel(String label) {
        return new BaseMatcher<EGroumNode>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof EGroumActionNode && ((EGroumActionNode) item).getLabel().equals(label);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an action node with label ");
                description.appendValue(label);
            }
        };
    }

    public static Matcher<? super EGroumNode> dataNodeWithLabel(String label) {
        return new BaseMatcher<EGroumNode>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof EGroumDataNode && ((EGroumDataNode) item).getLabel().equals(label);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a data node with label ");
                description.appendValue(label);
            }
        };
    }

    public static Matcher<? super AUG> hasSelEdge(Matcher<? super EGroumNode> sourceMatcher,
                                                  Matcher<? super EGroumNode> targetMatcher) {
        return hasEdge(new EdgeMatcher(sourceMatcher, "sel", targetMatcher));
    }

    public static Matcher<? super AUG> hasRepeatEdge(Matcher<? super EGroumNode> sourceMatcher,
                                                  Matcher<? super EGroumNode> targetMatcher) {
        return hasEdge(new EdgeMatcher(sourceMatcher, "rep", targetMatcher));
    }

    public static Matcher<? super AUG> hasSynchronizeEdge(Matcher<? super EGroumNode> sourceMatcher,
                                                     Matcher<? super EGroumNode> targetMatcher) {
        return hasEdge(new EdgeMatcher(sourceMatcher, "syn", targetMatcher));
    }

    public static Matcher<? super AUG> hasEdge(final Matcher<? super EGroumNode> sourceMatcher,
                                               final EGroumDataEdge.Type edgeType,
                                               final Matcher<? super EGroumNode> targetMatcher) {
        return hasEdge(new EdgeMatcher(sourceMatcher, EGroumDataEdge.getLabel(edgeType), targetMatcher));
    }

    private static Matcher<? super AUG> hasEdge(Matcher<? super EGroumEdge> matcher) {
        return new AUGElementMatcher<>(AbstractBaseGraph::edgeSet, matcher);
    }

    private static class AUGElementMatcher<E> extends BaseMatcher<AUG> {
        private final static AUGDotExporter augDotExporter = new AUGDotExporter(
                EGroumNode::getLabel, new AUGNodeAttributeProvider(), new AUGEdgeAttributeProvider());

        private final Function<AUG, Set<E>> selector;
        private final Matcher<? super E> elementMatcher;

        AUGElementMatcher(Function<AUG, Set<E>> selector, Matcher<? super E> elementMatcher) {
            this.selector = selector;
            this.elementMatcher = elementMatcher;
        }

        @Override
        public boolean matches(Object item) {
            return item instanceof AUG && Matchers.hasItem(elementMatcher).matches(selector.apply((AUG) item));
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("an AUG containing ");
            description.appendDescriptionOf(elementMatcher);
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            if (item instanceof AUG) {
                description.appendText("was AUG: ").appendText(augDotExporter.toDotGraph((AUG) item));
            } else {
                super.describeMismatch(item, description);
            }
        }
    }

    private static class EdgeMatcher extends BaseMatcher<EGroumEdge> {
        private final Matcher<? super EGroumNode> sourceMatcher;
        private final String edgeLabel;
        private final Matcher<? super EGroumNode> targetMatcher;

        private EdgeMatcher(Matcher<? super EGroumNode> sourceMatcher,
                            String edgeLabel,
                            Matcher<? super EGroumNode> targetMatcher) {
            this.sourceMatcher = sourceMatcher;
            this.edgeLabel = edgeLabel;
            this.targetMatcher = targetMatcher;
        }

        @Override
        public boolean matches(Object item) {
            if (item instanceof EGroumDataEdge) {
                EGroumDataEdge edge = (EGroumDataEdge) item;
                return sourceMatcher.matches(edge.getSource())
                        && edge.getLabel().equals(edgeLabel)
                        && targetMatcher.matches(edge.getTarget());
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("an edge with label ").appendValue(edgeLabel).appendText(" from ");
            description.appendDescriptionOf(sourceMatcher).appendText(" to ").appendDescriptionOf(targetMatcher);
        }
    }
}
