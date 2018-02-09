package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

public class EncodeConditionJunctionTest {

    @Test
    public void encodesDisjunction() {
    	AUGConfiguration conf = new AUGConfiguration() {{ encodeConditionalOperators = true; }};
        APIUsageExample aug = buildAUG(
                "void m(String s) { if (s.isEmpty() || s.contains(\"foo\")) s.getBytes(); }",
                conf);

        if (conf.buildTransitiveDataEdges) {
            assertThat(aug, hasParameterEdge(actionNodeWith(label("String.isEmpty()")), actionNodeWith(label("<c>"))));
            assertThat(aug, hasParameterEdge(actionNodeWith(label("String.contains()")), actionNodeWith(label("<c>"))));
        }
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("<c>")), actionNodeWith(label("String.getBytes()"))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("String.isEmpty()")), actionNodeWith(label("String.getBytes()"))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("String.contains()")), actionNodeWith(label("String.getBytes()"))));
    }

    @Test
    public void encodesConjunction() {
    	AUGConfiguration conf = new AUGConfiguration() {{ encodeConditionalOperators = true; }};
        APIUsageExample aug = buildAUG(
                "void m(String s) { if (s.isEmpty() && s.contains(\"foo\")) s.getBytes(); }",
                conf);
        
        if (conf.buildTransitiveDataEdges) {
            assertThat(aug, hasParameterEdge(actionNodeWith(label("String.isEmpty()")), actionNodeWith(label("<c>"))));
            assertThat(aug, hasParameterEdge(actionNodeWith(label("String.contains()")), actionNodeWith(label("<c>"))));
        }
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("<c>")), actionNodeWith(label("String.getBytes()"))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("String.isEmpty()")), actionNodeWith(label("String.getBytes()"))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("String.contains()")), actionNodeWith(label("String.getBytes()"))));
    }

    @Test
    public void ignoreJunctions() {
        APIUsageExample aug = buildAUG(
                "void m(String s) { if (s.isEmpty() || s.contains(\"foo\") && s.startsWith(\"bar\")) s.getBytes(); }",
                new AUGConfiguration() {{ encodeConditionalOperators = false; }});

        assertThat(aug, not(hasNode(actionNodeWith(label("<c>")))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("String.isEmpty()")), actionNodeWith(label("String.getBytes()"))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("String.contains()")), actionNodeWith(label("String.getBytes()"))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("String.startsWith()")), actionNodeWith(label("String.getBytes()"))));
    }
}
