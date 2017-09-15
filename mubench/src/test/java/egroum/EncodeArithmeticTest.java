package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static org.hamcrest.MatcherAssert.assertThat;

public class EncodeArithmeticTest {

    @Test
    public void notencodesArithmeticOperator() throws Exception {
    	AUGConfiguration conf = new AUGConfiguration() {{ encodeArithmeticOperators = false; }};
        AUG aug = buildAUG(
                "void m(String s) { if (s.length() + s.indexOf(\"foo\") > 0) s.getBytes(); }",
                conf);

        if (conf.buildTransitiveDataEdges) {
	        assertThat(aug, hasEdge(actionNodeWithLabel("String.length()"), PARAMETER, actionNodeWithLabel("<a>")));
	        assertThat(aug, hasEdge(actionNodeWithLabel("String.indexOf()"), PARAMETER, actionNodeWithLabel("<a>")));
        }
        assertThat(aug, notHaveNode(actionNodeWithLabel("<a>")));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("CharSequence.length()"), actionNodeWithLabel("String.getBytes()")));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("String.indexOf()"), actionNodeWithLabel("String.getBytes()")));
    }

    @Test
    public void encodesArithmeticOperator() throws Exception {
    	AUGConfiguration conf = new AUGConfiguration() {{ encodeArithmeticOperators = true; }};
        AUG aug = buildAUG(
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
