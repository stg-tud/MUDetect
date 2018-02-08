package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.*;
import static org.junit.Assert.assertThat;

public class EncodeSynchronizationTest {
    @Test
    public void addsSynchronizeEdge() {
        APIUsageExample aug = AUGBuilderTestUtils.buildAUG("void m(Object o) {" +
                "  synchronized (o) {" +
                "    o.equals();" +
                "  }" +
                "}");

        assertThat(aug, hasSynchronizeEdge(dataNodeWithLabel("Object"), actionNodeWithLabel("Object.equals()")));
    }
}
