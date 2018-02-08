package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGNodeMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.actionNodeWithLabel;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeInstanceOfTest {
    @Test
    public void encodesInstanceOf() {
        APIUsageExample aug = buildAUG("boolean isList(Object o) { return o instanceof java.util.List; }");

        assertThat(aug, hasNode(actionNodeWithLabel("List.<instanceof>")));
    }
}
