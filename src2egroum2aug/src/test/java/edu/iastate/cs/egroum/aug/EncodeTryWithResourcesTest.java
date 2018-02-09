package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.dataNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeTryWithResourcesTest {
    @Test
    public void desugarsTryWithResources() {
        APIUsageExample aug = buildAUG("void m() throws IOException {" +
                "  try (SomeResource r = new SomeResource()) {}" +
                "}");

        assertThat(aug, hasNode(actionNodeWith(label("AutoCloseable.close()"))));
        assertThat(aug, hasReceiverEdge(dataNodeWith(label("SomeResource")), actionNodeWith(label("AutoCloseable.close()"))));
        assertThat(aug, hasFinallyEdge(actionNodeWith(label("SomeResource.<init>")), actionNodeWith(label("AutoCloseable.close()"))));
    }
}
