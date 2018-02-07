package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.dataNodeWithName;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.dataNodeWithValue;
import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.MatcherAssert.assertThat;

public class EncodeLiteralsTest {
    @Test
    public void encodesIntLiteralWithValue() {
        APIUsageExample aug = buildAUG("int m() { return 42; }");

        assertThat(aug, hasNode(both(dataNodeWithType("int"))
                .and(dataNodeWithValue("42"))
                .and(dataNodeWithName(null))));
    }

    @Test
    public void encodesDoubleLiteralWithValue() {
        APIUsageExample aug = buildAUG("double m() { return 42.1; }");

        assertThat(aug, hasNode(both(dataNodeWithType("double"))
                .and(dataNodeWithValue("42.1"))
                .and(dataNodeWithName(null))));
    }

    @Test
    public void encodesStringLiteralWithValue() {
        APIUsageExample aug = buildAUG("String m() { return \"It's me!\"; }");

        assertThat(aug, hasNode(both(dataNodeWithType("String"))
                .and(dataNodeWithValue("It's me!"))
                .and(dataNodeWithName(null))));
    }

    @Test
    public void encodesClassLiteralWithName() {
        APIUsageExample aug = buildAUG("Class m() { return List.class; }");

        assertThat(aug, hasNode(both(dataNodeWithType("Class"))
                .and(dataNodeWithName("List.class"))
                .and(dataNodeWithValue(null))));
    }
}
