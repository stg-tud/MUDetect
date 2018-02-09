package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeInstanceOfTest {
    @Test
    public void encodesInstanceOf() {
        APIUsageExample aug = buildAUG("boolean isList(Object o) { return o instanceof java.util.List; }");

        assertThat(aug, hasNode(actionNodeWith(label("List.<instanceof>"))));
    }
}
