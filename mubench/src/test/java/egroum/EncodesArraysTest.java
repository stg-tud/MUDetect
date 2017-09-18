package egroum;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasNode;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodesArraysTest {
    @Test
    public void addsArrayCreation() throws Exception {
        APIUsageExample aug = buildAUG("void m() { int[] is = new int[42]; }");

        assertThat(aug, hasNode(actionNodeWithLabel("{int}")));
    }

    @Test
    public void addsArrayCreationInitializer() throws Exception {
        APIUsageExample aug = buildAUG("void m() { int[] is = new int[] { 1, 2 }; }");

        assertThat(aug, hasNode(actionNodeWithLabel("{int}")));
    }
}
