package mining;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import egroum.EGroumGraph;
import egroum.EGroumNode;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static de.tu_darmstadt.stg.mudetect.model.PatternTestUtils.isPattern;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static egroum.EGroumDataEdge.Type;
import static egroum.EGroumTestUtils.buildGroumsForClass;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static utils.CollectionUtils.first;

public class AUGMinerTest {
    @Test
    public void findsPattern() throws Exception {
        List<EGroumGraph> groums = buildGroumsForClass("class A {" +
                "  void m(C c) { c.foo(); }" +
                "  void n(C c) { c.foo(); }" +
                "}");

        Set<de.tu_darmstadt.stg.mudetect.model.Pattern> patterns = minePatterns(groums);

        assertThat(patterns, contains(isPattern(buildAUG().withActionNode("C.foo()"), 2)));
    }

    @Test
    public void findsDataNode() throws Exception {
        List<EGroumGraph> groums = buildGroumsForClass("class A {" +
                "  void m(C c) { c.foo(\"literal\"); }" +
                "  void n(C c) { c.foo(\"literal\"); }" +
                "}");

        Set<de.tu_darmstadt.stg.mudetect.model.Pattern> patterns = minePatterns(groums);

        AUG patternAUG = buildAUG().withActionNode("C.foo()").withDataNode("String")
                .withDataEdge("String", Type.PARAMETER, "C.foo()").build();
        assertThat(patterns, contains(isPattern(patternAUG, 2)));
    }

    @Test
    public void findsDataLiterals() throws Exception {
        List<EGroumGraph> groums = buildGroumsForClass("class A {" +
                "  void m(C c) { c.foo(\"l1\"); }" +
                "  void n(C c) { c.foo(\"l2\"); }" +
                "}");

        Set<de.tu_darmstadt.stg.mudetect.model.Pattern> patterns = minePatterns(groums);

        Pattern pattern = first(patterns);
        assertThat(pattern.getLiterals(node("String", pattern)), contains("l1", "l2"));
    }

    private Set<Pattern> minePatterns(List<EGroumGraph> groums) {
        Configuration config = new Configuration() {{ minPatternSupport = 2; extendSourceDataNodes = false; }};
        return new AUGMiner(config).mine(groums);
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
