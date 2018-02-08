package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.hasEdge;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.FINALLY;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeFinallyTest {
    @Test
    public void finallyEdge() {
        APIUsageExample aug = buildAUG("void close(java.io.InputStream is) throws IOException {" +
                "  try {" +
                "    is.read();" +
                "  } finally {" +
                "    is.close();" +
                "  }" +
                "}");

        assertThat(aug, hasEdge(actionNodeWithLabel("InputStream.read()"), FINALLY, actionNodeWithLabel("AutoCloseable.close()")));
    }
}
