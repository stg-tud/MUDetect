package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.MatcherAssert.assertThat;

public class EncodeConstantTest {

    @Test
    public void encodesPrimitiveConstantWithNameAndValue() {
        APIUsageExample aug = buildAUG("void m() { return Integer.MAX_VALUE; }");

        assertThat(aug, hasNode(both(dataNodeWithType("int"))
                .and(dataNodeWithName("Integer.MAX_VALUE"))
                .and(dataNodeWithValue(String.valueOf(Integer.MAX_VALUE)))));
    }

    @Test
    public void encodesObjectConstantWithName() {
        APIUsageExample aug = buildAUG("java.awt.Color m() { return java.awt.Color.BLACK; }");

        assertThat(aug, hasNode(both(dataNodeWithType("Color"))
                .and(dataNodeWithName("Color.BLACK"))
                .and(dataNodeWithValue(null))));
    }
}
