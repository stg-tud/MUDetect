package egroum;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.FINALLY;
import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.RECEIVER;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeTryWithResourcesTest {
    @Test
    public void desugarsTryWithResources() throws Exception {
        APIUsageExample aug = buildAUG("void m() throws IOException {" +
                "  try (SomeResource r = new SomeResource()) {}" +
                "}");

        assertThat(aug, hasNode(actionNodeWithLabel("AutoCloseable.close()")));
        assertThat(aug, hasEdge(dataNodeWithLabel("SomeResource"), RECEIVER, actionNodeWithLabel("AutoCloseable.close()")));
        assertThat(aug, hasEdge(actionNodeWithLabel("SomeResource.<init>"), FINALLY, actionNodeWithLabel("AutoCloseable.close()")));
    }
}
