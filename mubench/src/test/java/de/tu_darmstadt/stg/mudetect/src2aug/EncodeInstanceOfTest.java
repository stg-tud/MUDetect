package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasNode;
import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeInstanceOfTest {
    @Test
    public void encodesInstanceOf() throws Exception {
        APIUsageExample aug = buildAUG("boolean isList(Object o) { return o instanceof java.util.List; }");

        assertThat(aug, hasNode(actionNodeWithLabel("List.<instanceof>")));
    }
}
