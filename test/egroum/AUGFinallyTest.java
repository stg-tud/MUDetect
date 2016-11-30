package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasEdge;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumDataEdge.Type.FINALLY;
import static org.junit.Assert.assertThat;

public class AUGFinallyTest {
    @Test
    public void finallyEdge() throws Exception {
        AUG aug = buildAUG("void close(java.io.InputStream is) throws IOException {" +
                "  try {" +
                "    is.read();" +
                "  } finally {" +
                "    is.close();" +
                "  }" +
                "}");

        assertThat(aug, hasEdge(actionNodeWithLabel("InputStream.read()"), FINALLY, actionNodeWithLabel("AutoCloseable.close()")));
    }
}
