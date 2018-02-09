package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeSubExpressionsOrderTest {
	
    @Test
    public void encodeSubExpressionsOrderTest() {
        APIUsageExample aug = buildAUG("boolean m(java.util.List l) {\n" +
                "  return l != null && l.isEmpty();\n" +
                "}");

        assertThat(aug, hasParameterEdge(dataNodeWith(label("boolean")), actionNodeWith(label("return"))));
        assertThat(aug, hasDefinitionEdge(actionNodeWith(label("<nullcheck>")), dataNodeWith(label("boolean"))));
        assertThat(aug, hasDefinitionEdge(actionNodeWith(label("Collection.isEmpty()")), dataNodeWith(label("boolean"))));
        assertThat(aug, not(hasOrderEdge(actionNodeWith(label("<nullcheck>")), actionNodeWith(label("Collection.isEmpty()")))));
    }
	
    @Test
    public void encodeSubExpressionsControlTest1() {
        APIUsageExample aug = buildAUG("boolean m(java.util.List l) {\n" +
                "  return l != null && l.isEmpty();\n" +
                "}");

        assertThat(aug, hasParameterEdge(dataNodeWith(label("boolean")), actionNodeWith(label("return"))));
        assertThat(aug, hasDefinitionEdge(actionNodeWith(label("<nullcheck>")), dataNodeWith(label("boolean"))));
        assertThat(aug, hasDefinitionEdge(actionNodeWith(label("Collection.isEmpty()")), dataNodeWith(label("boolean"))));
        assertThat(aug, not(hasOrderEdge(actionNodeWith(label("<nullcheck>")), actionNodeWith(label("Collection.isEmpty()")))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("<nullcheck>")), actionNodeWith(label("Collection.isEmpty()"))));
    }

    @Test
    public void encodeSubExpressionsControlTest2() {
        APIUsageExample aug = buildAUG("boolean m(java.util.List l) {\n" +
                "  boolean b = l != null;\n" +
                "  return b && l.isEmpty();\n" +
                "}");

        assertThat(aug, hasParameterEdge(dataNodeWith(label("boolean")), actionNodeWith(label("return"))));
        assertThat(aug, hasDefinitionEdge(actionNodeWith(label("<nullcheck>")), dataNodeWith(label("boolean"))));
        assertThat(aug, hasDefinitionEdge(actionNodeWith(label("Collection.isEmpty()")), dataNodeWith(label("boolean"))));
        assertThat(aug, not(hasOrderEdge(actionNodeWith(label("<nullcheck>")), actionNodeWith(label("Collection.isEmpty()")))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("<nullcheck>")), actionNodeWith(label("Collection.isEmpty()"))));
    }

}
