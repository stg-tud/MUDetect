package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumDataEdge.Type.FINALLY;
import static egroum.EGroumDataEdge.Type.RECEIVER;
import static org.junit.Assert.assertThat;

public class EncodeTryWithResourcesTest {
    @Test
    public void desugarsTryWithResources() throws Exception {
        AUG aug = buildAUG("void m() throws IOException {" +
                "  try (SomeResource r = new SomeResource()) {}" +
                "}");

        assertThat(aug, hasNode(actionNodeWithLabel("AutoCloseable.close()")));
        assertThat(aug, hasEdge(dataNodeWithLabel("SomeResource"), RECEIVER, actionNodeWithLabel("AutoCloseable.close()")));
        assertThat(aug, hasEdge(actionNodeWithLabel("SomeResource.<init>"), FINALLY, actionNodeWithLabel("AutoCloseable.close()")));
    }
}
