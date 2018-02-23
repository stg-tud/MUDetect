package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.extend;
import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.SELECTION;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.*;
import static de.tu_darmstadt.stg.mudetect.model.TestViolationBuilder.someViolation;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class GenerateTargetEnvironmentDotGraphTest {
    @Test
    public void keepsSingleNode() {
        APIUsageExample aug = buildAUG().withActionNode("A").build();
        Overlap instance = instance(aug);

        String dotGraph = toDotGraph(someViolation(instance));

        assertDotGraphContains(dotGraph, "label=\"A\"");
    }

    @Test
    public void addsSameEdgeOnlyOnce() {
        APIUsageExample aug = buildAUG().withActionNodes("A", "B").withEdge("A", ORDER, "B").build();
        Overlap instance = instance(aug);

        String dotGraph = toDotGraph(someViolation(instance));

        assertThat(dotGraph, containsStringOnce("->"));
    }

    @Test
    public void excludesDistantNodes() {
        TestAUGBuilder pattern = buildAUG().withActionNode("A");
        TestAUGBuilder env = extend(pattern).withActionNode("B").withEdge("A", ORDER, "B");
        TestAUGBuilder target = extend(env).withActionNode("C").withEdge("B", ORDER, "C");
        TestOverlapBuilder instance = buildOverlap(pattern, target).withNode("A", "A");

        String dotGraph = toDotGraph(someViolation(instance));

        assertThat(dotGraph, not(containsString("label=\"C\"")));
    }

    @Test
    public void graysOutTargetOnlyElements() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A");
        TestAUGBuilder target = extend(pattern).withActionNode("B").withEdge("A", SELECTION, "B");
        TestOverlapBuilder instance = buildOverlap(pattern, target).withNode("A", "A");

        String dotGraph = toDotGraph(someViolation(instance));

        assertDotGraphContains(dotGraph, " [ label=\"B\" shape=\"box\" color=\"gray\" fontcolor=\"gray\" ];");
        assertDotGraphContains(dotGraph, " [ label=\"sel\" style=\"bold\" color=\"gray\" fontcolor=\"gray\" ];");
    }

    @Test
    public void highlightsMappedElements() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withEdge("A", ORDER, "B");
        TestOverlapBuilder instance = buildOverlap(pattern, target).withNodes("A", "B").withEdge("A", ORDER, "B");

        String dotGraph = toDotGraph(someViolation(instance));

        assertDotGraphContains(dotGraph, " [ label=\"A\" shape=\"box\" ];");
        assertDotGraphContains(dotGraph, " [ label=\"order\" style=\"bold\" ];");
    }

    @Test
    public void includesEdgesBetweenTargetOnlyNodes() {
        TestAUGBuilder pattern = buildAUG().withActionNode("A");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B", "C")
                .withEdge("A", SELECTION, "B")
                .withEdge("A", SELECTION, "C")
                .withEdge("B", PARAMETER, "C");
        TestOverlapBuilder instance = buildOverlap(pattern, target).withNode("A");

        String dotGraph = toDotGraph(someViolation(instance));

        assertDotGraphContains(dotGraph, "[ label=\"para\" style=\"solid\" color=\"gray\" fontcolor=\"gray\" ];");
    }

    @Test
    public void excludesOrderOnlyConnections() {
        TestAUGBuilder pattern = buildAUG().withActionNode("A");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withEdge("A", ORDER, "B");
        TestOverlapBuilder instance = buildOverlap(pattern, target).withNode("A");

        String dotGraph = toDotGraph(someViolation(instance));

        assertThat(dotGraph, not(containsString("label=\"order\"")));
        assertThat(dotGraph, not(containsString("label=\"B\"")));
    }

    /**
     * The rendering of environment graphs can take ages, since they may get large. Therefore, we limit the number of
     * iterations performed in graph layouting, since it doesn't make much of a difference for graphs of this size
     * anyways.
     */
    @Test
    public void setsNSLimitAttribute() {
        String dotGraph = toDotGraph(someViolation(someOverlap()));

        assertDotGraphContains(dotGraph, "nslimit=10000;");
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
