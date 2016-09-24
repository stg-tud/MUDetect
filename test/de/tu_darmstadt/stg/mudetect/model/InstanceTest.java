package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.Instance;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.InstanceTestUtils.contains;
import static egroum.EGroumDataEdge.Type.ORDER;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static org.junit.Assert.assertThat;

public class InstanceTest {
    @Test
    public void encodesMultipleEdgesBetweenTwoNodes() throws Exception {
        TestAUGBuilder builder = buildAUG().withActionNodes("A", "B")
                .withDataEdge("A", PARAMETER, "B")
                .withDataEdge("A", ORDER, "B");
        AUG aug = builder.build();
        Instance instance = new Instance(aug, aug.vertexSet(), aug.edgeSet());

        assertThat(instance, contains(builder.getEdge("A", ORDER, "B")));
        assertThat(instance, contains(builder.getEdge("A", PARAMETER, "B")));
    }
}
