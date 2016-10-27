package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.emptyInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.fullInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
import static egroum.EGroumDataEdge.Type.ORDER;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class GenerateTargetDotGraphTest {
    @Test
    public void includesTargetOnlyNode() throws Exception {
        Pattern pattern = somePattern(buildAUG());
        AUG target = buildAUG().withActionNode(":action:").build();
        Violation violation = new Violation(emptyInstance(pattern, target), 1);

        assertTargetDotGraphContains(violation,
                " [ label=\":action:\" shape=\"box\" color=\"gray\" fontcolor=\"gray\" ];");
    }

    @Test
    public void includesTargetOnlyEdge() throws Exception {
        Pattern pattern = somePattern(buildAUG());
        AUG target = buildAUG(":G:").withActionNodes(":a:", ":b:").withDataEdge(":a:", ORDER, ":b:").build();
        Violation violation = new Violation(emptyInstance(pattern, target), 1);

        assertTargetDotGraphContains(violation,
                " [ label=\"order\" style=\"dotted\" color=\"gray\" fontcolor=\"gray\" ];");
    }

    @Test
    public void includesMappedElements() throws Exception {
        AUG aug = buildAUG(":G:").withActionNodes(":a:", ":b:").withDataEdge(":a:", ORDER, ":b:").build();
        Violation violation = new Violation(fullInstance(aug), 1);

        assertTargetDotGraphContains(violation, " [ label=\":b:\" shape=\"box\" ");
        assertTargetDotGraphContains(violation, " [ label=\"order\" style=\"dotted\" ");
    }

    private void assertTargetDotGraphContains(Violation violation, String expectedDotGraph) {
        String dotGraph = new ViolationDotExporter().toTargetDotGraph(violation);

        assertThat(dotGraph, containsString(expectedDotGraph));
    }
}
