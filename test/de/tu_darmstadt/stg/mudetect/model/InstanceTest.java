package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.Instance;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.InstanceTestUtils.contains;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static egroum.EGroumDataEdge.Type.ORDER;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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

    @Test
    public void equalByMappedNode() throws Exception {
        final TestAUGBuilder augBuilder = buildAUG().withActionNode("a");
        final Instance instance1 = buildInstance(augBuilder, augBuilder).withNode("a", "a").build();
        final Instance instance2 = buildInstance(augBuilder, augBuilder).withNode("a", "a").build();

        assertEquals(instance1, instance2);
    }

    @Test
    public void equalByMappedEdge() throws Exception {
        final TestAUGBuilder augBuilder = buildAUG().withActionNodes("a", "b").withDataEdge("a", ORDER, "b");
        final Instance instance1 = buildInstance(augBuilder, augBuilder)
                .withNode("a", "a").withNode("b", "b").withEdge("a", "a", ORDER, "b", "b").build();
        final Instance instance2 = buildInstance(augBuilder, augBuilder)
                .withNode("a", "a").withNode("b", "b").withEdge("a", "a", ORDER, "b", "b").build();

        assertEquals(instance1, instance2);
    }

    @Test
    public void differByMappedNode() throws Exception {
        final TestAUGBuilder augBuilder = buildAUG().withActionNode("a");
        final Instance instance1 = buildInstance(augBuilder, augBuilder).withNode("a", "a").build();
        final Instance instance2 = buildInstance(augBuilder, augBuilder).build();

        assertNotEquals(instance1, instance2);
    }

    @Test
    public void differByMappedEdge() throws Exception {
        final TestAUGBuilder augBuilder = buildAUG().withActionNodes("a", "b").withDataEdge("a", ORDER, "b");
        final Instance instance1 = buildInstance(augBuilder, augBuilder)
                .withNode("a", "a").withNode("b", "b").withEdge("a", "a", ORDER, "b", "b").build();
        final Instance instance2 = buildInstance(augBuilder, augBuilder).withNode("a", "a").withNode("b", "b").build();

        assertNotEquals(instance1, instance2);
    }

    @Test
    public void differByMapping() throws Exception {
        final TestAUGBuilder augBuilder = buildAUG().withActionNodes("a", "b");
        final Instance instance1 = buildInstance(augBuilder, augBuilder).withNode("a", "a").withNode("b", "b").build();
        final Instance instance2 = buildInstance(augBuilder, augBuilder).withNode("a", "b").withNode("b", "a").build();

        assertNotEquals(instance1, instance2);
    }
}
