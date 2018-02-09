package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.name;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.type;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.value;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.MatcherAssert.assertThat;

public class EncodeConstantTest {

    @Test
    public void encodesPrimitiveConstantWithNameAndValue() {
        APIUsageExample aug = buildAUG("void m() { return Integer.MAX_VALUE; }");

        assertThat(aug, hasNode(dataNodeWith(both(type("int")).and(name("Integer.MAX_VALUE"))
                .and(value(String.valueOf(Integer.MAX_VALUE))))));
    }

    @Test
    public void encodesObjectConstantWithName() {
        APIUsageExample aug = buildAUG("java.awt.Color m() { return java.awt.Color.BLACK; }");

        assertThat(aug, hasNode(dataNodeWith(both(type("Color")).and(name("Color.BLACK")).and(value(null)))));
    }
}
