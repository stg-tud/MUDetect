package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.dataNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeExceptionHandlingTest {
    @Test
    public void encodeHandling() {
        APIUsageExample aug = buildAUG("void m() { try { hashCode(); } catch(RuntimeException e) { e.printStackTrace(); } }");

        assertThat(aug, hasThrowEdge(actionNodeWith(label("Object.hashCode()")), dataNodeWith(label("RuntimeException"))));
        assertThat(aug, hasParameterEdge(dataNodeWith(label("RuntimeException")), actionNodeWith(label("RuntimeException.<catch>"))));
        assertThat(aug, hasExceptionHandlingEdge(actionNodeWith(label("RuntimeException.<catch>")), actionNodeWith(label("Throwable.printStackTrace()"))));
        assertThat(aug, hasReceiverEdge(dataNodeWith(label("RuntimeException")), actionNodeWith(label("Throwable.printStackTrace()"))));
    }
}
