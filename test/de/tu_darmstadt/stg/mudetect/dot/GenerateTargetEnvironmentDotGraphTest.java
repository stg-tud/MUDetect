package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.*;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.extend;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.instance;
import static de.tu_darmstadt.stg.mudetect.model.TestViolationBuilder.someViolation;
import static egroum.EGroumDataEdge.Type.ORDER;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class GenerateTargetEnvironmentDotGraphTest {
    @Test
    public void keepsSingleNode() throws Exception {
        AUG aug = buildAUG().withActionNode("A").build();
        Overlap instance = instance(aug);

        String dotGraph = toDotGraph(someViolation(instance));

        assertDotGraphContains(dotGraph, "label=\"A\"");
    }

    @Test
    public void addsSameEdgeOnlyOnce() throws Exception {
        AUG aug = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B").build();
        Overlap instance = instance(aug);

        String dotGraph = toDotGraph(someViolation(instance));

        assertThat(dotGraph, containsStringOnce("->"));
    }

    @Test
    public void excludesDistantNodes() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNode("A");
        TestAUGBuilder env = extend(pattern).withActionNode("B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = extend(env).withActionNode("C").withDataEdge("B", ORDER, "C");
        TestOverlapBuilder instance = buildOverlap(target, pattern).withNode("A", "A");

        String dotGraph = toDotGraph(someViolation(instance));

        assertThat(dotGraph, not(containsString("label=\"C\"")));
    }

    @Test
    public void graysOutTargetOnlyElements() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A");
        TestAUGBuilder target = extend(pattern).withActionNode("B").withDataEdge("A", ORDER, "B");
        TestOverlapBuilder instance = buildOverlap(target, pattern).withNode("A", "A");

        String dotGraph = toDotGraph(someViolation(instance));

        assertDotGraphContains(dotGraph, " [ label=\"B\" shape=\"box\" color=\"gray\" fontcolor=\"gray\" ];");
        assertDotGraphContains(dotGraph, " [ label=\"order\" style=\"dotted\" color=\"gray\" fontcolor=\"gray\" ];");
    }

    @Test
    public void highlightsMappedElements() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestOverlapBuilder instance = buildOverlap(target, pattern).withNodes("A", "B").withEdge("A", ORDER, "B");

        String dotGraph = toDotGraph(someViolation(instance));

        assertDotGraphContains(dotGraph, " [ label=\"A\" shape=\"box\" ];");
        assertDotGraphContains(dotGraph, " [ label=\"order\" style=\"dotted\" ];");
    }

    @Test
    public void includesEdgesBetweenTargetOnlyNodes() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNode("A");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B", "C")
                .withDataEdge("A", ORDER, "B")
                .withDataEdge("A", ORDER, "C")
                .withDataEdge("B", PARAMETER, "C");
        TestOverlapBuilder instance = buildOverlap(target, pattern).withNode("A");

        String dotGraph = toDotGraph(someViolation(instance));

        assertDotGraphContains(dotGraph, "[ label=\"para\" style=\"dotted\" color=\"gray\" fontcolor=\"gray\" ];");
    }

    private String toDotGraph(Violation violation) {
        return new ViolationDotExporter().toTargetEnvironmentDotGraph(violation);
    }

    private static Matcher<String> containsStringOnce(String substring) {
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof String) {
                    String string = (String) item;
                    int firstIndexOfSubstring = string.indexOf(substring);
                    int lastIndexOfSubstring = string.lastIndexOf(substring);
                    return firstIndexOfSubstring != -1 && firstIndexOfSubstring == lastIndexOfSubstring;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("string containing ");
                description.appendValue(substring);
                description.appendText(" exactly once");
            }
        };
    }

    private void assertDotGraphContains(String dotGraph, String substring) {
        assertThat(dotGraph, containsString(substring));
    }
}
