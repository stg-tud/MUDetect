package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.*;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.extend;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.fullInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestViolationBuilder.someViolation;
import static egroum.EGroumDataEdge.Type.ORDER;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class GenerateTargetEnvironmentDotGraphTest {
    @Test
    public void keepsSingleNode() throws Exception {
        AUG aug = buildAUG().withActionNode("A").build();
        Instance instance = fullInstance(aug);

        String dotGraph = toDotGraph(someViolation(instance));

        assertThat(dotGraph, containsString("label=\"A\""));
    }

    @Test
    public void addSameEdgeOnlyOnce() throws Exception {
        AUG aug = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B").build();
        Instance instance = fullInstance(aug);

        String dotGraph = toDotGraph(someViolation(instance));

        assertThat(dotGraph, containsStringOnce("->"));
    }

    @Test
    public void excludesDistantNodes() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNode("A");
        TestAUGBuilder env = extend(pattern).withActionNode("B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = extend(env).withActionNode("C").withDataEdge("B", ORDER, "C");
        TestInstanceBuilder instance = buildInstance(target, pattern).withNode("A", "A");

        String dotGraph = toDotGraph(instance);

        assertThat(dotGraph, not(containsString("label=\"C\"")));
    }

    private String toDotGraph(TestInstanceBuilder instance) {
        return toDotGraph(someViolation(instance));
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
}
