package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.Node;
import de.tu_darmstadt.stg.mudetect.aug.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.aug.patterns.AggregateDataNode;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import java.util.Collection;
import java.util.Set;

import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.RECEIVER;
import static de.tu_darmstadt.stg.mudetect.mining.PatternTestUtils.isPattern;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUGsForClass;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static de.tu_darmstadt.stg.mudetect.utils.CollectionUtils.first;

public class DefaultAUGMinerTest {
    @Test
    public void findsPattern() throws Exception {
        Collection<APIUsageExample> groums = buildAUGsForClass("class A {" +
                "  void m(C c) { c.foo(); }" +
                "  void n(C c) { c.foo(); }" +
                "}");

        Set<APIUsagePattern> patterns = minePatterns(groums);

        TestAUGBuilder pattern = buildAUG().withDataNode("C").withActionNode("C.foo()")
                .withDataEdge("C", RECEIVER, "C.foo()");
        assertThat(patterns, contains(isPattern(pattern, 2)));
    }

    @Test
    public void findsDataNode() throws Exception {
        Collection<APIUsageExample> groums = buildAUGsForClass("class A {" +
                "  void m(C c) { c.foo(\"literal\"); }" +
                "  void n(C c) { c.foo(\"literal\"); }" +
                "}");

        Set<APIUsagePattern> patterns = minePatterns(groums);

        TestAUGBuilder pattern = buildAUG().withActionNode("C.foo()").withDataNodes("C", "String")
                .withDataEdge("C", RECEIVER, "C.foo()")
                .withDataEdge("String", PARAMETER, "C.foo()");
        assertThat(patterns, contains(isPattern(pattern, 2)));
    }

    @Test
    public void findsDataLiterals() throws Exception {
        Collection<APIUsageExample> groums = buildAUGsForClass("class A {" +
                "  void m(C c) { c.foo(\"l1\"); }" +
                "  void n(C c) { c.foo(\"l2\"); }" +
                "}");

        Set<APIUsagePattern> patterns = minePatterns(groums);

        APIUsagePattern pattern = first(patterns);
        assertThat(node("String", pattern).getDataNames(), contains("l1", "l2"));
    }

    @Test
    public void includesDataTypeInLiterals() throws Exception {
        Collection<APIUsageExample> groums = buildAUGsForClass("class A {" +
                "  void m(C c, String s) { c.foo(s); }" +
                "  void n(C c) { c.foo(\"literal\"); }" +
                "}");

        Set<APIUsagePattern> patterns = minePatterns(groums);

        APIUsagePattern pattern = first(patterns);
        assertThat(node("String", pattern).getDataNames(), containsInAnyOrder("literal", "s"));
    }

    @Test
    public void includesVariableNodeOccurrencesInLiterals() throws Exception {
        Collection<APIUsageExample> groums = buildAUGsForClass("class A {" +
                "  void m(C c, String s) { c.foo(s); }" +
                "  void n(C c, String s) { c.foo(s); }" +
                "}");

        Set<APIUsagePattern> patterns = minePatterns(groums);

        APIUsagePattern pattern = first(patterns);
        assertThat(node("String", pattern).getDataNames(), contains("s", "s"));
    }

    private Set<APIUsagePattern> minePatterns(Collection<APIUsageExample> groums) {
        Configuration config = new Configuration() {{ minPatternSupport = 2; extendSourceDataNodes = true; }};
        return new DefaultAUGMiner(config).mine(groums).getPatterns();
    }

    private AggregateDataNode node(String label, APIUsagePattern aug) {
        for (Node node : aug.vertexSet()) {
            if (node.getLabel().equals(label) && node instanceof AggregateDataNode) {
                return (AggregateDataNode) node;
            }
        }
        throw new IllegalArgumentException("no such node '" + label + "' in " + aug);
    }
}
