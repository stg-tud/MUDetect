package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Ignore;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.dataNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUGForMethod;
import static org.junit.Assert.assertThat;

public class EncodeExceptionHandlingTest {
    @Test
    public void encodeHandling() {
        APIUsageExample aug = buildAUGForMethod("void m() { try { hashCode(); } catch(RuntimeException e) { e.printStackTrace(); } }");

        assertThat(aug, hasThrowEdge(actionNodeWith(label("Object.hashCode()")), dataNodeWith(label("RuntimeException"))));
        assertThat(aug, hasParameterEdge(dataNodeWith(label("RuntimeException")), actionNodeWith(label("<catch>"))));
        assertThat(aug, hasExceptionHandlingEdge(actionNodeWith(label("<catch>")), actionNodeWith(label("Throwable.printStackTrace()"))));
        assertThat(aug, hasReceiverEdge(dataNodeWith(label("RuntimeException")), actionNodeWith(label("Throwable.printStackTrace()"))));
    }

    @Test
    @Ignore("This edge is currently not generated. I find this inconsistent, but have to think more about it -- Sven")
    public void throwCatchImpliesOrder() {
        APIUsageExample aug = buildAUGForMethod("void m() { try { hashCode(); } catch(RuntimeException e) { e.printStackTrace(); } }");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("Object.hashCode()")), actionNodeWith(label("<catch>"))));
    }

    @Test
    public void catchHandleImpliesOrder() {
        APIUsageExample aug = buildAUGForMethod("void m() { try { hashCode(); } catch(RuntimeException e) { e.printStackTrace(); } }");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("<catch>")), actionNodeWith(label("Throwable.printStackTrace()"))));
    }
}
