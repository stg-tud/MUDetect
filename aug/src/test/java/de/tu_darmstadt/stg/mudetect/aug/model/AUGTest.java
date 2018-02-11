package de.tu_darmstadt.stg.mudetect.aug.model;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AUGTest {
    @Test
    public void encodesMultipleEdgesBetweenTwoNodes() {
        TestAUGBuilder builder = buildAUG().withActionNodes("A", "B")
                .withEdge("A", PARAMETER, "B")
                .withEdge("A", ORDER, "B");
        APIUsageExample aug = builder.build();

        assertThat(aug, containsEdge(builder.getEdge("A", ORDER, "B")));
        assertThat(aug, containsEdge(builder.getEdge("A", PARAMETER, "B")));
    }

    @Test
    public void collectsAPIs() {
        APIUsageExample aug = buildAUG().withDataNodes("API1", "API2", "int").withActionNode("noAPI").build();

        assertThat(aug.getAPIs(), containsInAnyOrder("API1", "API2"));
    }

    @Test
    public void excludesNullFromAPIs() {
        APIUsageExample aug = buildAUG().withDataNode("null").build();

        assertThat(aug.getAPIs(), is(empty()));
    }

    @Test
    public void collectsAPIsFromActionNodes() {
        APIUsageExample aug = buildAUG().withActionNode("API.method()").build();

        assertThat(aug.getAPIs(), contains("API"));
    }

    @Test
    public void excludesArraysFromAPIs() {
        APIUsageExample aug = buildAUG().withDataNode("A[]").withActionNode("B[].length").build();

        assertThat(aug.getAPIs(), is(empty()));
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
