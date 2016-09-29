package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static egroum.EGroumDataEdge.Type.ORDER;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GenerateTargetDotGraphTest {
    @Test
    public void includesTargetOnlyNode() throws Exception {
        AUG pattern = buildAUG().build();
        AUG target = buildAUG(":G:").withActionNode(":action:").build();
        Violation violation = new Violation(new Instance(pattern, target), 1);

        assertTargetDotGraph(violation, "digraph \":G:\" {\n" +
                "  1 [ label=\":action:\" ];\n" +
                "}\n");
    }

    @Test
    public void includesTargetOnlyEdgeLabel() throws Exception {
        AUG pattern = buildAUG().build();
        AUG target = buildAUG(":G:").withActionNodes(":a:", ":b:").withDataEdge(":a:", ORDER, ":b:").build();
        Violation violation = new Violation(new Instance(pattern, target), 1);

        assertTargetDotGraph(violation, "digraph \":G:\" {\n" +
                "  1 [ label=\":b:\" ];\n" +
                "  2 [ label=\":a:\" ];\n" +
                "  2 -> 1 [ label=\"order\" ];\n" +
                "}\n");
    }

    @Test
    public void highlightsMappedNode() throws Exception {
        AUG aug = buildAUG(":G:").withActionNode(":action:").build();
        Violation violation = new Violation(new Instance(aug, aug.vertexSet(), aug.edgeSet()), 1);

        assertTargetDotGraph(violation, "digraph \":G:\" {\n" +
                "  1 [ label=\":action:\" color=\"blue\" fontcolor=\"blue\" ];\n" +
                "}\n");
    }

    @Test
    public void highlightsMappedEdge() throws Exception {
        AUG aug = buildAUG(":G:").withActionNodes(":a:", ":b:").withDataEdge(":a:", ORDER, ":b:").build();
        Violation violation = new Violation(new Instance(aug, aug.vertexSet(), aug.edgeSet()), 1);

        assertTargetDotGraph(violation, "digraph \":G:\" {\n" +
                "  1 [ label=\":b:\" color=\"blue\" fontcolor=\"blue\" ];\n" +
                "  2 [ label=\":a:\" color=\"blue\" fontcolor=\"blue\" ];\n" +
                "  2 -> 1 [ label=\"order\" color=\"blue\" fontcolor=\"blue\" ];\n" +
                "}\n");
    }

    private void assertTargetDotGraph(Violation violation, String expectedDotGraph) {
        String dotGraph = violation.toTargetDotGraph();

        assertThat(dotGraph, is(expectedDotGraph));
    }
}
