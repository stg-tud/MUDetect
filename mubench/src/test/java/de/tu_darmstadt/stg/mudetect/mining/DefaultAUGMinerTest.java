package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.AggregateDataNode;
import org.junit.Test;

import java.util.Collection;
import java.util.Set;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.RECEIVER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.mining.PatternTestUtils.isPattern;
import static de.tu_darmstadt.stg.mudetect.utils.CollectionUtils.first;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUGsForClass;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class DefaultAUGMinerTest {
    @Test
    public void findsPattern() {
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
    public void findsDataNode() {
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
