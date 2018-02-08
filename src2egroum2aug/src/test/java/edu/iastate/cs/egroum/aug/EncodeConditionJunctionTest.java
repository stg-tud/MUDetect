package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGNodeMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.*;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;

public class EncodeConditionJunctionTest {

    @Test
    public void encodesDisjunction() {
    	AUGConfiguration conf = new AUGConfiguration() {{ encodeConditionalOperators = true; }};
        APIUsageExample aug = buildAUG(
                "void m(String s) { if (s.isEmpty() || s.contains(\"foo\")) s.getBytes(); }",
                conf);

        if (conf.buildTransitiveDataEdges) {
	        MatcherAssert.assertThat(aug, hasEdge(actionNodeWithLabel("String.isEmpty()"), PARAMETER, actionNodeWithLabel("<c>")));
	        MatcherAssert.assertThat(aug, hasEdge(actionNodeWithLabel("String.contains()"), PARAMETER, actionNodeWithLabel("<c>")));
        }
        MatcherAssert.assertThat(aug, hasSelEdge(actionNodeWithLabel("<c>"), actionNodeWithLabel("String.getBytes()")));
        MatcherAssert.assertThat(aug, hasSelEdge(actionNodeWithLabel("String.isEmpty()"), actionNodeWithLabel("String.getBytes()")));
        MatcherAssert.assertThat(aug, hasSelEdge(actionNodeWithLabel("String.contains()"), actionNodeWithLabel("String.getBytes()")));
    }

    @Test
    public void encodesConjunction() {
    	AUGConfiguration conf = new AUGConfiguration() {{ encodeConditionalOperators = true; }};
        APIUsageExample aug = buildAUG(
                "void m(String s) { if (s.isEmpty() && s.contains(\"foo\")) s.getBytes(); }",
                conf);
        
        if (conf.buildTransitiveDataEdges) {
        	MatcherAssert.assertThat(aug, hasEdge(actionNodeWithLabel("String.isEmpty()"), PARAMETER, actionNodeWithLabel("<c>")));
        	MatcherAssert.assertThat(aug, hasEdge(actionNodeWithLabel("String.contains()"), PARAMETER, actionNodeWithLabel("<c>")));
        }
        MatcherAssert.assertThat(aug, hasSelEdge(actionNodeWithLabel("<c>"), actionNodeWithLabel("String.getBytes()")));
        MatcherAssert.assertThat(aug, hasSelEdge(actionNodeWithLabel("String.isEmpty()"), actionNodeWithLabel("String.getBytes()")));
        MatcherAssert.assertThat(aug, hasSelEdge(actionNodeWithLabel("String.contains()"), actionNodeWithLabel("String.getBytes()")));
    }

    @Test
    public void ignoreJunctions() {
        APIUsageExample aug = buildAUG(
                "void m(String s) { if (s.isEmpty() || s.contains(\"foo\") && s.startsWith(\"bar\")) s.getBytes(); }",
                new AUGConfiguration() {{ encodeConditionalOperators = false; }});

        MatcherAssert.assertThat(aug, Matchers.not(hasNode(actionNodeWithLabel("<c>"))));
        MatcherAssert.assertThat(aug, hasSelEdge(actionNodeWithLabel("String.isEmpty()"), actionNodeWithLabel("String.getBytes()")));
        MatcherAssert.assertThat(aug, hasSelEdge(actionNodeWithLabel("String.contains()"), actionNodeWithLabel("String.getBytes()")));
        MatcherAssert.assertThat(aug, hasSelEdge(actionNodeWithLabel("String.startsWith()"), actionNodeWithLabel("String.getBytes()")));
    }
}
