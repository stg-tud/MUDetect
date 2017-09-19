package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.dataNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasSynchronizeEdge;
import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeSynchronizationTest {
    @Test
    public void addsSynchronizeEdge() throws Exception {
        APIUsageExample aug = AUGBuilderTestUtils.buildAUG("void m(Object o) {" +
                "  synchronized (o) {" +
                "    o.equals();" +
                "  }" +
                "}");

        assertThat(aug, hasSynchronizeEdge(dataNodeWithLabel("Object"), actionNodeWithLabel("Object.equals()")));
    }
}
