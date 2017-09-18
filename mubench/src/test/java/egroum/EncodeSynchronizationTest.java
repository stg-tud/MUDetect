package egroum;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.dataNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasSynchronizeEdge;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeSynchronizationTest {
    @Test
    public void addsSynchronizeEdge() throws Exception {
        APIUsageExample aug = buildAUG("void m(Object o) {" +
                "  synchronized (o) {" +
                "    o.equals();" +
                "  }" +
                "}");

        assertThat(aug, hasSynchronizeEdge(dataNodeWithLabel("Object"), actionNodeWithLabel("Object.equals()")));
    }
}
