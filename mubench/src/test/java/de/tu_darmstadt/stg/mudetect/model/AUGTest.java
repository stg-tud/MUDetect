package de.tu_darmstadt.stg.mudetect.model;

import egroum.EGroumEdge;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static egroum.EGroumDataEdge.Type.ORDER;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class AUGTest {
    @Test
    public void encodesMultipleEdgesBetweenTwoNodes() throws Exception {
        TestAUGBuilder builder = buildAUG().withActionNodes("A", "B")
                .withDataEdge("A", PARAMETER, "B")
                .withDataEdge("A", ORDER, "B");
        AUG aug = builder.build();

        assertThat(aug, containsEdge(builder.getEdge("A", ORDER, "B")));
        assertThat(aug, containsEdge(builder.getEdge("A", PARAMETER, "B")));
    }

    @Test
    public void collectsAPIs() throws Exception {
        AUG aug = buildAUG().withDataNodes("API1", "API2", "int").withActionNode("noAPI").build();

        assertThat(aug.getAPIs(), containsInAnyOrder("API1", "API2"));
    }

    private Matcher<AUG> containsEdge(EGroumEdge edge) {
        return new BaseMatcher<AUG>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof AUG && ((AUG) item).edgeSet().contains(edge);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected AUG with edge ");
                description.appendValue(edge);
            }
        };
    }
}
