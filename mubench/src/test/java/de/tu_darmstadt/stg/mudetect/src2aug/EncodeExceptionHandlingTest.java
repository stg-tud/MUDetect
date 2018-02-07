package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.*;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeExceptionHandlingTest {
    @Test
    public void encodeHandling() throws Exception {
        APIUsageExample aug = buildAUG("void m() { try { hashCode(); } catch(RuntimeException e) { e.printStackTrace(); } }");

        assertThat(aug, hasEdge(actionNodeWithLabel("Object.hashCode()"), THROW, dataNodeWithLabel("RuntimeException")));
        assertThat(aug, hasEdge(dataNodeWithLabel("RuntimeException"), PARAMETER, actionNodeWithLabel("RuntimeException.<catch>")));
        assertThat(aug, hasEdge(actionNodeWithLabel("RuntimeException.<catch>"), EXCEPTION_HANDLING, actionNodeWithLabel("Throwable.printStackTrace()")));
        assertThat(aug, hasEdge(dataNodeWithLabel("RuntimeException"), RECEIVER, actionNodeWithLabel("Throwable.printStackTrace()")));
    }
}
