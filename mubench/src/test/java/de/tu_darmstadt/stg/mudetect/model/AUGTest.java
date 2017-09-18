package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.Edge;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class AUGTest {
    @Test
    public void encodesMultipleEdgesBetweenTwoNodes() throws Exception {
        TestAUGBuilder builder = buildAUG().withActionNodes("A", "B")
                .withDataEdge("A", PARAMETER, "B")
                .withDataEdge("A", ORDER, "B");
        APIUsageExample aug = builder.build();

        assertThat(aug, containsEdge(builder.getEdge("A", ORDER, "B")));
        assertThat(aug, containsEdge(builder.getEdge("A", PARAMETER, "B")));
    }

    @Test
    public void collectsAPIs() throws Exception {
        APIUsageExample aug = buildAUG().withDataNodes("API1", "API2", "int").withActionNode("noAPI").build();

        assertThat(aug.getAPIs(), containsInAnyOrder("API1", "API2"));
    }

    private Matcher<APIUsageExample> containsEdge(Edge edge) {
        return new BaseMatcher<APIUsageExample>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof APIUsageExample && ((APIUsageExample) item).edgeSet().contains(edge);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected AUG with edge ");
                description.appendValue(edge);
            }
        };
    }
}
