package de.tu_darmstadt.stg.mudetect.model;

import egroum.EGroumDataNode;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.Set;
import java.util.stream.Collectors;

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
        return new BaseMatcher<AUG>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof AUG) {
                    Set<EGroumNode> nodes = ((AUG) item).vertexSet();
                    return Matchers.hasItem(matcher).matches(nodes);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an AUG containing ");
                description.appendDescriptionOf(matcher);
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
}
