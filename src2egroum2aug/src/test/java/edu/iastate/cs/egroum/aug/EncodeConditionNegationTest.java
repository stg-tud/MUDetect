package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.hamcrest.Matchers;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGNodeMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.*;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeConditionNegationTest {
    private static final String NEGATED_CONDITION = "void m(java.util.List l) { if (!l.isEmpty()) l.get(0); }";

    @Test
    public void encodesNegation() {
    	AUGConfiguration conf = new AUGConfiguration() {{ encodeUnaryOperators = true; }};
        APIUsageExample aug = buildAUG(NEGATED_CONDITION, conf);

        if (conf.buildTransitiveDataEdges)
        	assertThat(aug, hasEdge(actionNodeWithLabel("Collection.isEmpty()"), PARAMETER, actionNodeWithLabel("!")));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("List.get()")));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("!"), actionNodeWithLabel("List.get()")));
    }

    @Test
    public void ignoresNegation() {
        APIUsageExample aug = buildAUG(NEGATED_CONDITION, new AUGConfiguration() {{ encodeUnaryOperators = false; }});

        assertThat(aug, Matchers.not(hasNode(actionNodeWithLabel("!"))));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("List.get()")));
    }
}
