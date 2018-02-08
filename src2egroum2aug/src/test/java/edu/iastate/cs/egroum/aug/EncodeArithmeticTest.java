package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGNodeMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.*;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

public class EncodeArithmeticTest {

    @Test
    public void notencodesArithmeticOperator() {
    	AUGConfiguration conf = new AUGConfiguration() {{ encodeArithmeticOperators = false; }};
        APIUsageExample aug = buildAUG(
                "void m(String s) { if (s.length() + s.indexOf(\"foo\") > 0) s.getBytes(); }",
                conf);

        if (conf.buildTransitiveDataEdges) {
	        assertThat(aug, hasEdge(actionNodeWithLabel("String.length()"), PARAMETER, actionNodeWithLabel("<a>")));
	        assertThat(aug, hasEdge(actionNodeWithLabel("String.indexOf()"), PARAMETER, actionNodeWithLabel("<a>")));
        }
        assertThat(aug, not(hasNode(actionNodeWithLabel("<a>"))));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("CharSequence.length()"), actionNodeWithLabel("String.getBytes()")));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("String.indexOf()"), actionNodeWithLabel("String.getBytes()")));
    }

    @Test
    public void encodesArithmeticOperator() {
    	AUGConfiguration conf = new AUGConfiguration() {{ encodeArithmeticOperators = true; }};
        APIUsageExample aug = buildAUG(
                "void m(String s) { if (s.length() + s.indexOf(\"foo\") > 0) s.getBytes(); }",
                conf);

        if (conf.buildTransitiveDataEdges) {
	        assertThat(aug, hasEdge(actionNodeWithLabel("String.length()"), PARAMETER, actionNodeWithLabel("<a>")));
	        assertThat(aug, hasEdge(actionNodeWithLabel("String.indexOf()"), PARAMETER, actionNodeWithLabel("<a>")));
        }
        assertThat(aug, hasSelEdge(actionNodeWithLabel("<a>"), actionNodeWithLabel("String.getBytes()")));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("CharSequence.length()"), actionNodeWithLabel("String.getBytes()")));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("String.indexOf()"), actionNodeWithLabel("String.getBytes()")));
    }
}
