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
                "}", null);

        Set<de.tu_darmstadt.stg.mudetect.model.Pattern> patterns = new AUGMiner(2, 1).mine(groums);

        assertThat(patterns, contains(isPattern(buildAUG().withActionNode("C.foo()"), 2)));
    }

    @Test
    public void findsDataNode() throws Exception {
        List<EGroumGraph> groums = buildGroumsForClass("class A {" +
                "  void m(C c) { c.foo(\"literal\"); }" +
                "  void n(C c) { c.foo(\"literal\"); }" +
                "}", null);

        Set<de.tu_darmstadt.stg.mudetect.model.Pattern> patterns = new AUGMiner(2, 1).mine(groums);

        AUG patternAUG = buildAUG().withActionNode("C.foo()").withDataNode("String")
                .withDataEdge("String", Type.PARAMETER, "C.foo()").build();
        assertThat(patterns, contains(isPattern(patternAUG, 2)));
    }

    @Test
    public void findsDataLiterals() throws Exception {
        List<EGroumGraph> groums = buildGroumsForClass("class A {" +
                "  void m(C c) { c.foo(\"l1\"); }" +
                "  void n(C c) { c.foo(\"l2\"); }" +
                "}", null);

        Set<de.tu_darmstadt.stg.mudetect.model.Pattern> patterns = new AUGMiner(2, 1).mine(groums);

        Pattern pattern = first(patterns);
        assertThat(pattern.getLiterals(node("String", pattern)), contains("l1", "l2"));
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
