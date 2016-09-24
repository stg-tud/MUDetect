package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.junit.Test;

import java.util.HashSet;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static egroum.EGroumDataEdge.Type.ORDER;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GenerateViolationDotGraphTest {

    @Test
    public void includesNodeLabel() throws Exception {
        AUG aug = buildAUG(":G:").withActionNode(":action:").build();
        Violation violation = new Violation(new Instance(aug, aug.vertexSet(), aug.edgeSet()));

        assertDotGraph(violation, "digraph \":G:\" {\n" +
                "  1 [ label=\":action:\" ];\n" +
                "}\n");
    }

    @Test
    public void includesEdgeLabel() throws Exception {
        AUG aug = buildAUG(":G:").withActionNodes(":a:", ":b:").withDataEdge(":a:", ORDER, ":b:").build();
        Violation violation = new Violation(new Instance(aug, aug.vertexSet(), aug.edgeSet()));

        assertDotGraph(violation, "digraph \":G:\" {\n" +
                "  1 [ label=\":b:\" ];\n" +
                "  2 [ label=\":a:\" ];\n" +
                "  2 -> 1 [ label=\"order\" ];\n" +
                "}\n");
    }

    @Test
    public void includesMissingNode() throws Exception {
        AUG aug = buildAUG(":G:").withActionNode(":action:").build();
        Violation violation = new Violation(new Instance(aug, new HashSet<>(), new HashSet<>()));

        assertDotGraph(violation, "digraph \":G:\" {\n" +
                "  1 [ label=\":action:\" color=\"red\" missing=\"true\" ];\n" +
                "}\n");
    }

    @Test
    public void includesMiddingEdge() throws Exception {
        AUG aug = buildAUG(":G:").withActionNodes(":a:", ":b:").withDataEdge(":a:", ORDER, ":b:").build();
        Violation violation = new Violation(new Instance(aug, aug.vertexSet(), new HashSet<>()));

        assertDotGraph(violation, "digraph \":G:\" {\n" +
                "  1 [ label=\":b:\" ];\n" +
                "  2 [ label=\":a:\" ];\n" +
                "  2 -> 1 [ label=\"order\" color=\"red\" missing=\"true\" ];\n" +
                "}\n");
    }

    private void assertDotGraph(Violation violation, String expectedDotGraph) {
        String dotGraph = violation.toDotGraph();

        assertThat(dotGraph, is(expectedDotGraph));
    }
}
