package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.emptyOverlap;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.instance;
import static de.tu_darmstadt.stg.mudetect.mining.TestPatternBuilder.somePattern;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class GenerateTargetDotGraphTest {
    @Test
    public void includesTargetOnlyNode() throws Exception {
        APIUsagePattern pattern = somePattern(buildAUG());
        APIUsageExample target = buildAUG().withActionNode(":action:").build();
        Violation violation = new Violation(emptyOverlap(pattern, target), 1, "constant rank");

        assertTargetDotGraphContains(violation,
                " [ label=\":action:\" shape=\"box\" color=\"gray\" fontcolor=\"gray\" ];");
    }

    @Test
    public void includesTargetOnlyEdge() throws Exception {
        APIUsagePattern pattern = somePattern(buildAUG());
        APIUsageExample target = buildAUG(":G:").withActionNodes(":a:", ":b:").withDataEdge(":a:", ORDER, ":b:").build();
        Violation violation = new Violation(emptyOverlap(pattern, target), 1, "constant rank");

        assertTargetDotGraphContains(violation,
                " [ label=\"order\" style=\"solid\" color=\"gray\" fontcolor=\"gray\" ];");
    }

    @Test
    public void includesMappedElements() throws Exception {
        APIUsageExample aug = buildAUG(":G:").withActionNodes(":a:", ":b:").withDataEdge(":a:", ORDER, ":b:").build();
        Violation violation = new Violation(instance(aug), 1, "constant rank");

        assertTargetDotGraphContains(violation, " [ label=\":b:\" shape=\"box\" ");
        assertTargetDotGraphContains(violation, " [ label=\"order\" style=\"solid\" ");
    }

    private void assertTargetDotGraphContains(Violation violation, String expectedDotGraph) {
        String dotGraph = new ViolationDotExporter().toTargetDotGraph(violation);

        assertThat(dotGraph, containsString(expectedDotGraph));
    }
}
