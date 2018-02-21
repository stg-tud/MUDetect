package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.dataNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUGForMethod;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeSubExpressionsControlFlowTest {
	
    @Test
    public void capturesSubexpressionOrder() {
        APIUsageExample aug = buildAUGForMethod("boolean m(java.util.List l) {\n" +
                "  return l != null && l.isEmpty();\n" +
                "}");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("<nullcheck>")), actionNodeWith(label("Collection.isEmpty()"))));
        assertThat(aug, not(hasOrderEdge(actionNodeWith(label("Collection.isEmpty()")), actionNodeWith(label("<nullcheck>")))));
    }
	
    @Test
    public void capturesSubexpressionControlRelation() {
        APIUsageExample aug = buildAUGForMethod("boolean m(java.util.List l) {\n" +
                "  return l != null && l.isEmpty();\n" +
                "}");

        assertThat(aug, hasSelectionEdge(actionNodeWith(label("<nullcheck>")), actionNodeWith(label("Collection.isEmpty()"))));
    }

    @Test
    public void capturesSubexpressionOrderAndControlRelationFromExtractedSubexpression() {
        APIUsageExample aug = buildAUGForMethod("boolean m(java.util.List l) {\n" +
                "  boolean b = l != null;\n" +
                "  return b && l.isEmpty();\n" +
                "}");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("<nullcheck>")), actionNodeWith(label("Collection.isEmpty()"))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("<nullcheck>")), actionNodeWith(label("Collection.isEmpty()"))));
    }

    @Test
    public void capturesSubexpressionOrderRelationFromExtractedSubexpressions() {
        APIUsageExample aug = buildAUGForMethod("boolean m(java.util.List l) {\n" +
                "  boolean b = l != null;\n" +
                "  boolean c = l.isEmpty();\n" +
                "  return b && c;\n" +
                "}");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("<nullcheck>")), actionNodeWith(label("Collection.isEmpty()"))));
        assertThat(aug, not(hasSelectionEdge(actionNodeWith(label("<nullcheck>")), actionNodeWith(label("Collection.isEmpty()")))));
    }

    @Test
    public void capturesOrderToSubexpressions() {
        APIUsageExample aug = buildAUGForMethod("void m(int timestamp) {\n" +
                "  java.io.File filrep = new java.io.File();\n" +
                "  return ((filrep != null) && filrep.exists());" +
                "}");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("File.<init>")), actionNodeWith(label("File.exists()"))));
        assertThat(aug, hasOrderEdge(actionNodeWith(label("File.<init>")), actionNodeWith(label("<nullcheck>"))));
    }

    @Test
    public void capturesOrderWithTernaryOperator() {
        APIUsageExample aug = buildAUGForMethod("Object m(Object o) {\n" +
                "  return o.equals(\"foo\") ? o.hashCode() : o.wait();\n" +
                "}");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("Object.equals()")), actionNodeWith(label("Object.hashCode()"))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("Object.equals()")), actionNodeWith(label("Object.hashCode()"))));
        assertThat(aug, hasOrderEdge(actionNodeWith(label("Object.equals()")), actionNodeWith(label("Object.wait()"))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("Object.equals()")), actionNodeWith(label("Object.wait()"))));
        assertThat(aug, not(hasOrderEdge(actionNodeWith(label("Object.hashCode()")), actionNodeWith(label("Object.wait()")))));
        assertThat(aug, not(hasOrderEdge(actionNodeWith(label("Object.<init>")), actionNodeWith(label("Object.hashCode()")))));
    }
}
