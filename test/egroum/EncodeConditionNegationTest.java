package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasSelEdge;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeConditionNegationTest {
    private static final String NEGATED_CONDITION = "void m(java.util.List l) { if (!l.isEmpty()) l.get(0); }";

    @Test
    public void encodesNegation() throws Exception {
    	AUGConfiguration conf = new AUGConfiguration() {{ encodeUnaryOperators = true; }};
        AUG aug = buildAUG(NEGATED_CONDITION, conf);

        if (conf.buildTransitiveDataEdges)
        	assertThat(aug, hasEdge(actionNodeWithLabel("Collection.isEmpty()"), PARAMETER, actionNodeWithLabel("!")));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("List.get()")));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("!"), actionNodeWithLabel("List.get()")));
    }

    @Test
    public void ignoresNegation() throws Exception {
        AUG aug = buildAUG(NEGATED_CONDITION, new AUGConfiguration() {{ encodeUnaryOperators = false; }});

        assertThat(aug, not(hasNode(actionNodeWithLabel("!"))));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("List.get()")));
    }
}
