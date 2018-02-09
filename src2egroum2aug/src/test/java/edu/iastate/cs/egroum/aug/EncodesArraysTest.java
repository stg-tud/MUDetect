package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodesArraysTest {
    @Test
    public void addsArrayCreation() {
        APIUsageExample aug = buildAUG("void m() { int[] is = new int[42]; }");

        assertThat(aug, hasNode(actionNodeWith(label("{int}"))));
    }

    @Test
    public void addsArrayCreationInitializer() {
        APIUsageExample aug = buildAUG("void m() { int[] is = new int[] { 1, 2 }; }");

        assertThat(aug, hasNode(actionNodeWith(label("{int}"))));
    }

    @Test
    public void addsArrayAccess() {
        APIUsageExample aug = buildAUG("int m(int[] is) { return is[0]; }");

        assertThat(aug, hasNode(actionNodeWith(label("int[].arrayget()"))));
    }

    @Test
    public void addsArrayAssignment() {
        APIUsageExample aug = buildAUG("void m(int[] is, int i) { is[0] = i; }");

        assertThat(aug, hasNode(actionNodeWith(label("int[].arrayset()"))));
    }
}
