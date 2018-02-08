package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.*;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.*;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeSubExpressionsOrderTest {
	
    @Test
    public void encodeSubExpressionsOrderTest() {
        APIUsageExample aug = buildAUG("boolean m(java.util.List l) {\n" +
                "  return l != null && l.isEmpty();\n" +
                "}");

        assertThat(aug, hasEdge(dataNodeWithLabel("boolean"), PARAMETER, actionNodeWithLabel("return")));
        assertThat(aug, hasEdge(actionNodeWithLabel("<nullcheck>"), DEFINITION, dataNodeWithLabel("boolean")));
        assertThat(aug, hasEdge(actionNodeWithLabel("Collection.isEmpty()"), DEFINITION, dataNodeWithLabel("boolean")));
        assertThat(aug, not(hasEdge(actionNodeWithLabel("<nullcheck>"), ORDER, actionNodeWithLabel("Collection.isEmpty()"))));
    }
	
    @Test
    public void encodeSubExpressionsControlTest1() {
        APIUsageExample aug = buildAUG("boolean m(java.util.List l) {\n" +
                "  return l != null && l.isEmpty();\n" +
                "}");

        assertThat(aug, hasEdge(dataNodeWithLabel("boolean"), PARAMETER, actionNodeWithLabel("return")));
        assertThat(aug, hasEdge(actionNodeWithLabel("<nullcheck>"), DEFINITION, dataNodeWithLabel("boolean")));
        assertThat(aug, hasEdge(actionNodeWithLabel("Collection.isEmpty()"), DEFINITION, dataNodeWithLabel("boolean")));
        assertThat(aug, not(hasEdge(actionNodeWithLabel("<nullcheck>"), ORDER, actionNodeWithLabel("Collection.isEmpty()"))));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("<nullcheck>"), actionNodeWithLabel("Collection.isEmpty()")));
    }

    @Test
    public void encodeSubExpressionsControlTest2() {
        APIUsageExample aug = buildAUG("boolean m(java.util.List l) {\n" +
                "  boolean b = l != null;\n" +
                "  return b && l.isEmpty();\n" +
                "}");

        assertThat(aug, hasEdge(dataNodeWithLabel("boolean"), PARAMETER, actionNodeWithLabel("return")));
        assertThat(aug, hasEdge(actionNodeWithLabel("<nullcheck>"), DEFINITION, dataNodeWithLabel("boolean")));
        assertThat(aug, hasEdge(actionNodeWithLabel("Collection.isEmpty()"), DEFINITION, dataNodeWithLabel("boolean")));
        assertThat(aug, not(hasEdge(actionNodeWithLabel("<nullcheck>"), ORDER, actionNodeWithLabel("Collection.isEmpty()"))));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("<nullcheck>"), actionNodeWithLabel("Collection.isEmpty()")));
    }

}
