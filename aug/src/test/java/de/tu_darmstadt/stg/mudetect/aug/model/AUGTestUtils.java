package de.tu_darmstadt.stg.mudetect.aug.model;

import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.AUGDotExporter;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.AUGEdgeAttributeProvider;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.AUGNodeAttributeProvider;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jgrapht.graph.AbstractBaseGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.REPETITION;
import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.SELECTION;
import static org.hamcrest.core.AllOf.allOf;

public class AUGTestUtils {
    public static Matcher<? super APIUsageGraph> isEqual(APIUsageGraph expected) {
        Set<String> expectedNodeLabels = getNodeLabels(expected);
        Set<String> expectedEdgeLabels = getEdgeLabels(expected);

        return new BaseMatcher<APIUsageGraph>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof APIUsageGraph) {
                    APIUsageGraph actual = (APIUsageGraph) item;
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

    private static Set<String> getNodeLabels(APIUsageGraph expected) {
        Set<String> expectedNodeLabels = expected.vertexSet().stream()
                .map(Node::getLabel).collect(Collectors.toSet());
        if (expectedNodeLabels.size() < expected.getNodeSize()) {
            throw new IllegalArgumentException("cannot handle AUG with multiple equally-labelled nodes");
        }
        return expectedNodeLabels;
    }

    private static Set<String> getEdgeLabels(APIUsageGraph aug) {
        Set<String> expectedEdgeLabels = aug.edgeSet().stream()
                .map(AUGTestUtils::getEdgeLabel).collect(Collectors.toSet());
        if (expectedEdgeLabels.size() < aug.getEdgeSize()) {
            throw new IllegalArgumentException("cannot handle AUG with multiple equally-labelled edges between the same nodes");
        }
        return expectedEdgeLabels;
    }

    private static String getEdgeLabel(Edge edge) {
        return edge.getSource().getLabel() + "--(" + edge.getLabel() + ")-->" + edge.getTarget().getLabel();
    }

    public static Matcher<? super APIUsageGraph> hasNode(Matcher<? super Node> matcher) {
        return new AUGElementMatcher<>(AbstractBaseGraph::vertexSet, matcher);
    }

    @SafeVarargs
    public static Matcher<? super APIUsageGraph> hasNodes(Matcher<? super Node>... matchers) {
        List<Matcher<? super APIUsageGraph>> all = new ArrayList<>(matchers.length);

        for (Matcher<? super Node> matcher : matchers) {
            all.add(hasNode(matcher));
        }

        return allOf(all);
    }

    public static Matcher<? super Node> actionNodeWithLabel(String label) {
        return new BaseMatcher<Node>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof ActionNode && ((ActionNode) item).getLabel().equals(label);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a ").appendValue(label).appendText(" action node");
            }
        };
    }

    public static Matcher<? super Node> dataNodeWithLabel(String label) {
        return new BaseMatcher<Node>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof DataNode && ((DataNode) item).getLabel().equals(label);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a ").appendValue(label).appendText(" data node");
            }
        };
    }
    
    public static Matcher<Node> dataNodeWithType(String type) {
        return new BaseMatcher<Node>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof DataNode && Objects.equals(((DataNode) item).getType(), type);
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText("a data node with type ").appendValue(type);
            }
        };
    }
    
    public static Matcher<Node> dataNodeWithName(String name) {
        return new BaseMatcher<Node>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof DataNode && Objects.equals(((DataNode) item).getName(), name);
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText("a data node with name ").appendValue(name);
            }
        };
    }
    
    public static Matcher<Node> dataNodeWithValue(String value) {
        return new BaseMatcher<Node>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof DataNode && Objects.equals(((DataNode) item).getValue(), value);
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText("a data node with value ").appendValue(value);
            }
        };
    }

    public static Matcher<? super APIUsageGraph> hasSelEdge(Matcher<? super Node> sourceMatcher,
                                                              Matcher<? super Node> targetMatcher) {
        return hasEdge(new ConditionEdgeMatcher(sourceMatcher, SELECTION, targetMatcher));
    }

    public static Matcher<? super APIUsageGraph> hasRepeatEdge(Matcher<? super Node> sourceMatcher,
                                                                 Matcher<? super Node> targetMatcher) {
        return hasEdge(new ConditionEdgeMatcher(sourceMatcher, REPETITION, targetMatcher));
    }

    public static Matcher<? super APIUsageGraph> hasSynchronizeEdge(Matcher<? super Node> sourceMatcher,
                                                                      Matcher<? super Node> targetMatcher) {
        return hasEdge(new EdgeMatcher(sourceMatcher, Edge.Type.SYNCHRONIZE, targetMatcher));
    }
    
    public static Matcher<? super APIUsageExample> hasHandleEdge(Matcher<? super Node> sourceMatcher,
                                                                 Matcher<? super Node> targetMatcher) {
        return hasEdge(new EdgeMatcher(sourceMatcher, Edge.Type.CONDITION, targetMatcher));
    }

    public static Matcher<? super APIUsageGraph> hasEdge(final Matcher<? super Node> sourceMatcher,
                                                           final Edge.Type edgeType,
                                                           final Matcher<? super Node> targetMatcher) {
        return hasEdge(new EdgeMatcher(sourceMatcher, edgeType, targetMatcher));
    }

    private static Matcher<? super APIUsageGraph> hasEdge(Matcher<? super Edge> matcher) {
        return new AUGElementMatcher<>(AbstractBaseGraph::edgeSet, matcher);
    }

    private static class AUGElementMatcher<E> extends BaseMatcher<APIUsageGraph> {
        private final static AUGDotExporter augDotExporter = new AUGDotExporter(
                Node::getLabel, new AUGNodeAttributeProvider(), new AUGEdgeAttributeProvider());

        private final Function<APIUsageGraph, Set<E>> selector;
        private final Matcher<? super E> elementMatcher;

        AUGElementMatcher(Function<APIUsageGraph, Set<E>> selector, Matcher<? super E> elementMatcher) {
            this.selector = selector;
            this.elementMatcher = elementMatcher;
        }

        @Override
        public boolean matches(Object item) {
            return item instanceof APIUsageGraph && Matchers.hasItem(elementMatcher).matches(selector.apply((APIUsageGraph) item));
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("an AUG containing ");
            description.appendDescriptionOf(elementMatcher);
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            if (item instanceof APIUsageGraph) {
                description.appendText("was AUG: ").appendText(augDotExporter.toDotGraph((APIUsageGraph) item));
            } else {
                super.describeMismatch(item, description);
            }
        }
    }

    private static class EdgeMatcher extends BaseMatcher<Edge> {
        final Matcher<? super Node> sourceMatcher;
        final Edge.Type type;
        final Matcher<? super Node> targetMatcher;

        private EdgeMatcher(Matcher<? super Node> sourceMatcher,
                            Edge.Type type,
                            Matcher<? super Node> targetMatcher) {
            this.sourceMatcher = sourceMatcher;
            this.type = type;
            this.targetMatcher = targetMatcher;
        }

        @Override
        public boolean matches(Object item) {
            if (item instanceof Edge) {
                Edge edge = (Edge) item;
                return sourceMatcher.matches(edge.getSource())
                        && edge.getType() == type
                        && targetMatcher.matches(edge.getTarget());
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a ").appendValue(type.getLabel()).appendText(" edge from ");
            description.appendDescriptionOf(sourceMatcher).appendText(" to ").appendDescriptionOf(targetMatcher);
        }
    }

    private static class ConditionEdgeMatcher extends EdgeMatcher {
        private final ConditionEdge.ConditionType conditionType;

        private ConditionEdgeMatcher(Matcher<? super Node> sourceMatcher, ConditionEdge.ConditionType conditionType,
                                     Matcher<? super Node> targetMatcher) {
            super(sourceMatcher, Edge.Type.CONDITION, targetMatcher);
            this.conditionType = conditionType;
        }

        @Override
        public boolean matches(Object item) {
            if (super.matches(item)) {
                if (item instanceof TransitiveEdge) {
                    item = ((TransitiveEdge) item).getCorrespondingDirectEdge();
                }
                return ((ConditionEdge) item).getConditionType() == conditionType;
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a ").appendValue(type).appendText("/").appendValue(conditionType)
                    .appendText(" edge from ").appendDescriptionOf(sourceMatcher).appendText(" to ")
                    .appendDescriptionOf(targetMatcher);
        }
    }
}
