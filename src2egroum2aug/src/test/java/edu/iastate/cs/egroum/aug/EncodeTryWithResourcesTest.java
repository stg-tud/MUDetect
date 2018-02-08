package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGNodeMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.*;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.FINALLY;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.RECEIVER;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeTryWithResourcesTest {
    @Test
    public void desugarsTryWithResources() {
        APIUsageExample aug = buildAUG("void m() throws IOException {" +
                "  try (SomeResource r = new SomeResource()) {}" +
                "}");

        assertThat(aug, hasNode(actionNodeWithLabel("AutoCloseable.close()")));
        assertThat(aug, hasEdge(dataNodeWithLabel("SomeResource"), RECEIVER, actionNodeWithLabel("AutoCloseable.close()")));
        assertThat(aug, hasEdge(actionNodeWithLabel("SomeResource.<init>"), FINALLY, actionNodeWithLabel("AutoCloseable.close()")));
    }
}
