package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.dataNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasEdge;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumDataEdge.Type.*;
import static org.junit.Assert.assertThat;

public class EncodeSubExpressionsOrderTest {
    @Test
    public void encodeSubExpressionsOrderTest() throws Exception {
        AUG aug = buildAUG("boolean m(java.util.List l) {\n" +
                "  return l != null && l.isEmpty();\n" +
                "}");

        assertThat(aug, hasEdge(dataNodeWithLabel("boolean"), PARAMETER, actionNodeWithLabel("return")));
        assertThat(aug, hasEdge(actionNodeWithLabel("<nullcheck>"), DEFINITION, dataNodeWithLabel("boolean")));
        assertThat(aug, hasEdge(actionNodeWithLabel("Collection.isEmpty()"), DEFINITION, dataNodeWithLabel("boolean")));
        assertThat(aug, hasEdge(actionNodeWithLabel("<nullcheck>"), ORDER, actionNodeWithLabel("Collection.isEmpty()")));
    }
}
