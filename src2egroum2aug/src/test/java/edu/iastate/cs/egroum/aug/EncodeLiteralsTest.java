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

public class EncodeLiteralsTest {
    @Test
    public void encodesIntLiteralWithValue() {
        APIUsageExample aug = buildAUG("int m() { return 42; }");

        assertThat(aug, hasNode(dataNodeWith(both(type("int")).and(value("42")).and(name(null)))));
    }

    @Test
    public void encodesDoubleLiteralWithValue() {
        APIUsageExample aug = buildAUG("double m() { return 42.1; }");

        assertThat(aug, hasNode(dataNodeWith(both(type("double")).and(value("42.1")).and(name(null)))));
    }

    @Test
    public void encodesStringLiteralWithValue() {
        APIUsageExample aug = buildAUG("String m() { return \"It's me!\"; }");

        assertThat(aug, hasNode(dataNodeWith(both(type("String")).and(value("It's me!")).and(name(null)))));
    }

    @Test
    public void encodesClassLiteralWithName() {
        APIUsageExample aug = buildAUG("Class m() { return List.class; }");

        assertThat(aug, hasNode(dataNodeWith(both(type("Class")).and(name("List.class")).and(value(null)))));
    }
}
