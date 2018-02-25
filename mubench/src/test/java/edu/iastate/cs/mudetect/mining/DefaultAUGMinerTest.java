package edu.iastate.cs.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.*;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.AggregateDataNode;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.RECEIVER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static edu.iastate.cs.mudetect.mining.Configuration.DataNodeExtensionStrategy.IF_INCOMING;
import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.somePattern;
import static de.tu_darmstadt.stg.mudetect.utils.CollectionUtils.first;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUGsForClass;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class DefaultAUGMinerTest {
    private static final Configuration MINING_CONFIG = new Configuration() {{ minPatternSupport = 2; extendByDataNode = IF_INCOMING; }};

    @Test
    public void findsPattern() {
        Collection<APIUsageExample> groums = buildAUGsForClass("class A {" +
                "  void m(C c) { c.foo(); }" +
                "  void n(C c) { c.foo(); }" +
                "}");

        Set<APIUsagePattern> patterns = minePatterns(groums);

        TestAUGBuilder pattern = buildAUG().withDataNode("C").withActionNode("C.foo()")
                .withEdge("C", RECEIVER, "C.foo()");
        assertThat(patterns, contains(isPattern(pattern, 2)));
    }

    @Test
    public void findsDataNode() {
        Collection<APIUsageExample> groums = buildAUGsForClass("class A {" +
                "  void m(C c) { c.foo(\"literal\"); }" +
                "  void n(C c) { c.foo(\"literal\"); }" +
                "}");

        Set<APIUsagePattern> patterns = minePatterns(groums);

        TestAUGBuilder pattern = buildAUG().withActionNode("C.foo()").withDataNodes("C", "String")
                .withEdge("C", RECEIVER, "C.foo()")
                .withEdge("String", PARAMETER, "C.foo()");
        assertThat(patterns, contains(isPattern(pattern, 2)));
    }

    @Test
    public void findsDataLiterals() {
        Collection<APIUsageExample> groums = buildAUGsForClass("class A {" +
                "  void m(C c) { c.foo(\"l1\"); }" +
                "  void n(C c) { c.foo(\"l2\"); }" +
                "}");

        Set<APIUsagePattern> patterns = minePatterns(groums);

        APIUsagePattern pattern = first(patterns);
        assertThat(node("String", pattern).getAggregatedValues(), contains("l1", "l2"));
    }

    @Test
    public void includesDataTypeInLiterals() {
        Collection<APIUsageExample> groums = buildAUGsForClass("class A {" +
                "  void m(C c, String s) { c.foo(s); }" +
                "  void n(C c) { c.foo(\"literal\"); }" +
                "}");

        Set<APIUsagePattern> patterns = minePatterns(groums);

        APIUsagePattern pattern = first(patterns);
        AggregateDataNode stringNode = node("String", pattern);
        assertThat(stringNode.getAggregatedValues(), contains("literal"));
        assertThat(stringNode.getAggregatedNames(), contains("s"));
    }

    @Test
    public void includesVariableNodeOccurrencesInLiterals() {
        Collection<APIUsageExample> groums = buildAUGsForClass("class A {" +
                "  void m(C c, String s) { c.foo(s); }" +
                "  void n(C c, String s) { c.foo(s); }" +
                "}");

        Set<APIUsagePattern> patterns = minePatterns(groums);

        APIUsagePattern pattern = first(patterns);
        assertThat(node("String", pattern).getAggregatedNames(), contains("s", "s"));
    }

    private Set<APIUsagePattern> minePatterns(Collection<APIUsageExample> groums) {
        return new DefaultAUGMiner(MINING_CONFIG).mine(groums).getPatterns();
    }

    private AggregateDataNode node(String label, APIUsagePattern aug) {
        for (Node node : aug.vertexSet()) {
            if (MINING_CONFIG.labelProvider.getLabel(node).equals(label) && node instanceof AggregateDataNode) {
                return (AggregateDataNode) node;
            }
        }
        throw new IllegalArgumentException("no such node '" + label + "' in " + aug);
    }

    private static Matcher<APIUsagePattern> isPattern(TestAUGBuilder builder, int support) {
        return isPattern(builder.build(APIUsagePattern.class), support);
    }

    private static Matcher<APIUsagePattern> isPattern(APIUsagePattern aug, int support) {
        Matcher<? super APIUsageGraph> augMatcher = isEqual(aug);
        return new BaseMatcher<APIUsagePattern>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof APIUsagePattern) {
                    APIUsagePattern actual = (APIUsagePattern) item;
                    return support == actual.getSupport() &&
                            augMatcher.matches(actual);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(somePattern(aug, support));
            }
        };
    }

    private static Matcher<? super APIUsageGraph> isEqual(APIUsageGraph expected) {
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
                .map(MINING_CONFIG.labelProvider::getLabel).collect(Collectors.toSet());
        if (expectedNodeLabels.size() < expected.getNodeSize()) {
            throw new IllegalArgumentException("cannot handle AUG with multiple equally-labelled nodes");
        }
        return expectedNodeLabels;
    }

    private static Set<String> getEdgeLabels(APIUsageGraph aug) {
        Set<String> expectedEdgeLabels = aug.edgeSet().stream()
                .map(DefaultAUGMinerTest::getEdgeLabel).collect(Collectors.toSet());
        if (expectedEdgeLabels.size() < aug.getEdgeSize()) {
            throw new IllegalArgumentException("cannot handle AUG with multiple equally-labelled edges between the same nodes");
        }
        return expectedEdgeLabels;
    }

    private static String getEdgeLabel(Edge edge) {
        return MINING_CONFIG.labelProvider.getLabel(edge.getSource()) + "--(" +
                MINING_CONFIG.labelProvider.getLabel(edge) + ")-->" +
                MINING_CONFIG.labelProvider.getLabel(edge.getTarget());
    }

}
