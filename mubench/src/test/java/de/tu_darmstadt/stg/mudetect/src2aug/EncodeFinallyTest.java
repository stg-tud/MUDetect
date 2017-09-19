package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.FINALLY;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasEdge;
import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeFinallyTest {
    @Test
    public void finallyEdge() throws Exception {
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
