package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.Instance;
import egroum.EGroumEdge;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGBuilder.buildAUG;
import static egroum.EGroumDataEdge.Type.ORDER;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static org.junit.Assert.assertThat;

public class InstanceTest {
    @Test
    public void encodesMultipleEdgesBetweenTwoNodes() throws Exception {
        AUGBuilder builder = buildAUG().withActionNodes("A", "B")
                .withDataEdge("A", PARAMETER, "B")
                .withDataEdge("A", ORDER, "B");
        AUG aug = builder.build();
        Instance instance = new Instance(aug, aug.vertexSet(), aug.edgeSet());

        assertThat(instance, containsEdge(builder.getEdge("A", ORDER, "B")));
        assertThat(instance, containsEdge(builder.getEdge("A", PARAMETER, "B")));
    }

    private Matcher<Instance> containsEdge(EGroumEdge edge) {
        return new BaseMatcher<Instance>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof Instance && ((Instance) item).edgeSet().contains(edge);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected AUG with edge ");
                description.appendValue(edge);
            }
        };
    }
}
