package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
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

//    @Test
//    public void encodeSubExpressionsControlTest() throws Exception {
//        AUG aug = buildAUG("boolean m(java.util.List l) {\n" +
//                "  return l != null && l.isEmpty();\n" +
//                "}");
//
//        assertThat(aug, hasEdge(dataNodeWithLabel("boolean"), PARAMETER, actionNodeWithLabel("return")));
//        assertThat(aug, hasEdge(actionNodeWithLabel("<nullcheck>"), DEFINITION, dataNodeWithLabel("boolean")));
//        assertThat(aug, hasEdge(actionNodeWithLabel("Collection.isEmpty()"), DEFINITION, dataNodeWithLabel("boolean")));
//        assertThat(aug, hasEdge(actionNodeWithLabel("<nullcheck>"), ORDER, actionNodeWithLabel("Collection.isEmpty()")));
//        assertThat(aug, hasSelEdge(actionNodeWithLabel("<nullcheck>"), actionNodeWithLabel("Collection.isEmpty()")));
//    }
}
