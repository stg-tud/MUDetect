package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.*;
import org.junit.Test;

import java.util.HashSet;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class GenerateViolationDotGraphTest {

    @Test
    public void includesNodeLabel() {
        APIUsageExample aug = buildAUG(":G:").withActionNode(":action:").build();
        Violation violation = new Violation(instance(aug), 1, "constant rank");

        assertDotGraphContains(violation, " [ label=\":action:\" shape=\"box\" ");
    }

    @Test
    public void includesEdgeLabel() {
        APIUsageExample aug = buildAUG(":G:").withActionNodes(":a:", ":b:").withEdge(":a:", ORDER, ":b:").build();
        Violation violation = new Violation(instance(aug), 1, "constant rank");

        assertDotGraphContains(violation, " [ label=\"order\" style=\"bold\" ");
    }

    @Test
    public void includesMissingNode() {
        APIUsageExample aug = buildAUG().withActionNode(":action:").build();
        Violation violation = new Violation(emptyOverlap(aug), 1, "constant rank");

        assertDotGraphContains(violation, "1 [ label=\":action:\" shape=\"box\" color=\"red\" fontcolor=\"red\" ];");
    }

    @Test
    public void includesMissingEdge() {
        APIUsageExample aug = buildAUG().withActionNodes(":a:", ":b:").withEdge(":a:", ORDER, ":b:").build();
        Violation violation = new Violation(someOverlap(aug, aug.vertexSet(), new HashSet<>()), 1, "constant rank");

        assertDotGraphContains(violation, " [ label=\"order\" style=\"bold\" color=\"red\" fontcolor=\"red\" ];");
    }

    @Test
    public void rendersDataNodeAsEllipse() {
        final APIUsageExample aug = buildAUG().withDataNode("D").build();
        final Violation violation = new Violation(instance(aug), 1, "constant rank");

        assertDotGraphContains(violation, "[ label=\"D\" shape=\"ellipse\" ");
    }

    @Test
    public void usesTargetNodeLabelByDefault() {
        TestAUGBuilder target = buildAUG().withActionNode("A");
        TestAUGBuilder pattern = buildAUG().withActionNode("B");
        Overlap overlap = buildOverlap(pattern, target).withNode("A", "B").build();

        assertDotGraphContains(new Violation(overlap, 1, "constant rank"), " [ label=\"A\"");
    }

    @Test
    public void fallsBackToPatternNodeLabelForMissingNodes() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A");
        Overlap overlap = emptyOverlap(pattern);

        assertDotGraphContains(new Violation(overlap, 1, "constant rank"), " [ label=\"A\"");
    }

    private void assertDotGraphContains(Violation violation, String expectedDotGraphFragment) {
        String dotGraph = new ViolationDotExporter().toDotGraph(violation);

        assertThat(dotGraph, containsString(expectedDotGraphFragment));
    }
}
