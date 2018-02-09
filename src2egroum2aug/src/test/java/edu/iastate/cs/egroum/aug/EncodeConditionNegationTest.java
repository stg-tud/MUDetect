package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeConditionNegationTest {
    private static final String NEGATED_CONDITION = "void m(java.util.List l) { if (!l.isEmpty()) l.get(0); }";

    @Test
    public void encodesNegation() {
    	AUGConfiguration conf = new AUGConfiguration() {{ encodeUnaryOperators = true; }};
        APIUsageExample aug = buildAUG(NEGATED_CONDITION, conf);

        if (conf.buildTransitiveDataEdges)
            assertThat(aug, hasParameterEdge(actionNodeWith(label("Collection.isEmpty()")), actionNodeWith(label("!"))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("Collection.isEmpty()")), actionNodeWith(label("List.get()"))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("!")), actionNodeWith(label("List.get()"))));
    }

    @Test
    public void ignoresNegation() {
        APIUsageExample aug = buildAUG(NEGATED_CONDITION, new AUGConfiguration() {{ encodeUnaryOperators = false; }});

        assertThat(aug, not(hasNode(actionNodeWith(label("!")))));
        assertThat(aug, hasSelectionEdge(actionNodeWith(label("Collection.isEmpty()")), actionNodeWith(label("List.get()"))));
    }
}
