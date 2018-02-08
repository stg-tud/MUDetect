package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.dataNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGNodeMatchers.hasNode;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeDataNodesTest {
    @Test
    public void addsDataNodeForParameter() {
        APIUsageExample aug = buildAUG("void m(Object o) { o.hashCode(); }");

        assertThat(aug, hasNode(dataNodeWithLabel("Object")));
    }

    @Test
    public void addsDataNodeForVariable() {
        APIUsageExample aug = buildAUG("String m() { String self = toString(); return self; }");

        assertThat(aug, hasNode(dataNodeWithLabel("String")));
    }

    @Test
    public void addsDataNodeForImplicitReference() {
        APIUsageExample aug = buildAUG("String m() { return toString(); }");

        assertThat(aug, hasNode(dataNodeWithLabel("String")));
    }

    @Test
    public void noDataNodeForUnusedReturnValue() {
        APIUsageExample aug = buildAUG("void m() { toString(); }");

        assertThat(aug, not(hasNode(dataNodeWithLabel("String"))));
    }

    @Test
    public void noDataNodeForUnusedVariable() {
        APIUsageExample aug = buildAUG("void m() { String s = toString(); }");

        assertThat(aug, not(hasNode(dataNodeWithLabel("String"))));
    }

    @Test
    public void noDataNodeForUnusedLiteral() {
        APIUsageExample aug = buildAUG("void m() { String s = \"foo\"; }");

        assertThat(aug, not(hasNode(dataNodeWithLabel("String"))));
    }
}
