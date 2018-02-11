package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.InstanceTestUtils.contains;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

public class OverlapTest {
    @Test
    public void encodesMultipleEdgesBetweenTwoNodes() {
        TestAUGBuilder builder = buildAUG().withActionNodes("A", "B")
                .withEdge("A", PARAMETER, "B")
                .withEdge("A", ORDER, "B");
        APIUsageExample aug = builder.build();
        Overlap instance = instance(aug);

        assertThat(instance, contains(builder.getEdge("A", ORDER, "B")));
        assertThat(instance, contains(builder.getEdge("A", PARAMETER, "B")));
    }

    @Test
    public void equalByMappedNode() {
        final TestAUGBuilder augBuilder = buildAUG().withActionNode("a");
        final Overlap overlap1 = buildOverlap(augBuilder, augBuilder).withNode("a", "a").build();
        final Overlap overlap2 = buildOverlap(augBuilder, augBuilder).withNode("a", "a").build();

        assertEquals(overlap1, overlap2);
    }

    @Test
    public void equalByMappedEdge() {
        final TestAUGBuilder augBuilder = buildAUG().withActionNodes("a", "b").withEdge("a", ORDER, "b");
        final Overlap overlap1 = buildOverlap(augBuilder, augBuilder)
                .withNode("a", "a").withNode("b", "b").withEdge("a", "a", ORDER, "b", "b").build();
        final Overlap overlap2 = buildOverlap(augBuilder, augBuilder)
                .withNode("a", "a").withNode("b", "b").withEdge("a", "a", ORDER, "b", "b").build();

        assertEquals(overlap1, overlap2);
    }

    @Test
    public void differByMappedNode() {
        final TestAUGBuilder augBuilder = buildAUG().withActionNode("a");
        final Overlap overlap1 = buildOverlap(augBuilder, augBuilder).withNode("a", "a").build();
        final Overlap overlap2 = buildOverlap(augBuilder, augBuilder).build();

        assertNotEquals(overlap1, overlap2);
    }

    @Test
    public void differByMappedEdge() {
        final TestAUGBuilder augBuilder = buildAUG().withActionNodes("a", "b").withEdge("a", ORDER, "b");
        final Overlap overlap1 = buildOverlap(augBuilder, augBuilder)
                .withNode("a", "a").withNode("b", "b").withEdge("a", "a", ORDER, "b", "b").build();
        final Overlap overlap2 = buildOverlap(augBuilder, augBuilder).withNode("a", "a").withNode("b", "b").build();

        assertNotEquals(overlap1, overlap2);
    }

    @Test
    public void differByMapping() {
        final TestAUGBuilder augBuilder = buildAUG().withActionNodes("a", "b");
        final Overlap overlap1 = buildOverlap(augBuilder, augBuilder).withNode("a", "a").withNode("b", "b").build();
        final Overlap overlap2 = buildOverlap(augBuilder, augBuilder).withNode("a", "b").withNode("b", "a").build();

        assertNotEquals(overlap1, overlap2);
    }
}
