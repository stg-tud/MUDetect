package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import egroum.EGroumGraph;
import egroum.EGroumNode;
import mining.Configuration;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static de.tu_darmstadt.stg.mudetect.mining.PatternTestUtils.isPattern;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static egroum.EGroumDataEdge.Type.*;
import static egroum.EGroumTestUtils.buildGroumsForClass;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static utils.CollectionUtils.first;

public class DefaultAUGMinerTest {
    @Test
    public void findsPattern() throws Exception {
        List<EGroumGraph> groums = buildGroumsForClass("class A {" +
                "  void m(C c) { c.foo(); }" +
                "  void n(C c) { c.foo(); }" +
                "}");

        Set<Pattern> patterns = minePatterns(groums);

        TestAUGBuilder pattern = buildAUG().withDataNode("C").withActionNode("C.foo()")
                .withDataEdge("C", RECEIVER, "C.foo()");
        assertThat(patterns, contains(isPattern(pattern, 2)));
    }

    @Test
    public void findsDataNode() throws Exception {
        List<EGroumGraph> groums = buildGroumsForClass("class A {" +
                "  void m(C c) { c.foo(\"literal\"); }" +
                "  void n(C c) { c.foo(\"literal\"); }" +
                "}");

        Set<Pattern> patterns = minePatterns(groums);

        TestAUGBuilder pattern = buildAUG().withActionNode("C.foo()").withDataNodes("C", "String")
                .withDataEdge("C", RECEIVER, "C.foo()")
                .withDataEdge("String", PARAMETER, "C.foo()");
        assertThat(patterns, contains(isPattern(pattern, 2)));
    }

    @Test
    public void findsDataLiterals() throws Exception {
        List<EGroumGraph> groums = buildGroumsForClass("class A {" +
                "  void m(C c) { c.foo(\"l1\"); }" +
                "  void n(C c) { c.foo(\"l2\"); }" +
                "}");

        Set<Pattern> patterns = minePatterns(groums);

        Pattern pattern = first(patterns);
        assertThat(pattern.getLiterals(node("String", pattern)), contains("l1", "l2"));
    }

    @Test
    public void includesDataTypeInLiterals() throws Exception {
        List<EGroumGraph> groums = buildGroumsForClass("class A {" +
                "  void m(C c, String s) { c.foo(s); }" +
                "  void n(C c) { c.foo(\"literal\"); }" +
                "}");

        Set<Pattern> patterns = minePatterns(groums);

        Pattern pattern = first(patterns);
        assertThat(pattern.getLiterals(node("String", pattern)), containsInAnyOrder("literal", "s"));
    }

    @Test
    public void includesVariableNodeOccurrencesInLiterals() throws Exception {
        List<EGroumGraph> groums = buildGroumsForClass("class A {" +
                "  void m(C c, String s) { c.foo(s); }" +
                "  void n(C c, String s) { c.foo(s); }" +
                "}");

        Set<Pattern> patterns = minePatterns(groums);

        Pattern pattern = first(patterns);
        assertThat(pattern.getLiterals(node("String", pattern)), contains("s", "s"));
    }

    private Set<Pattern> minePatterns(List<EGroumGraph> groums) {
        Configuration config = new Configuration() {{ minPatternSupport = 2; extendSourceDataNodes = true; }};
        return new DefaultAUGMiner(config).mine(groums).getPatterns();
    }

    private EGroumNode node(String label, AUG aug) {
        for (EGroumNode node : aug.vertexSet()) {
            if (node.getLabel().equals(label)) {
                return node;
            }
        }
        throw new IllegalArgumentException("no such node '" + label + "' in " + aug);
    }
}
