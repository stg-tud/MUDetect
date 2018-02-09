package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasSynchronizeEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.dataNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static org.junit.Assert.assertThat;

public class EncodeSynchronizationTest {
    @Test
    public void addsSynchronizeEdge() {
        APIUsageExample aug = AUGBuilderTestUtils.buildAUG("void m(Object o) {" +
                "  synchronized (o) {" +
                "    o.equals();" +
                "  }" +
                "}");

        assertThat(aug, hasSynchronizeEdge(dataNodeWith(label("Object")), actionNodeWith(label("Object.equals()"))));
    }
}
