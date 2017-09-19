package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.CONDITION;
import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasEdge;
import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeCallOrderTest {
    @Test
    public void encodesTransitiveOrderEdges() throws Exception {
        APIUsageExample aug = AUGBuilderTestUtils.buildAUG("void m(java.util.List l) {\n" +
                "  l.add(null);\n" +
                "  l.get(0);\n" +
                "  l.clear();\n" +
                "}");
        
        assertThat(aug, hasEdge(actionNodeWithLabel("Collection.add()"), ORDER, actionNodeWithLabel("List.get()")));
        assertThat(aug, hasEdge(actionNodeWithLabel("Collection.add()"), ORDER, actionNodeWithLabel("Collection.clear()")));
        assertThat(aug, hasEdge(actionNodeWithLabel("List.get()"), ORDER, actionNodeWithLabel("Collection.clear()")));
        assertThat(aug, not(hasEdge(actionNodeWithLabel("Collection.add()"), CONDITION, actionNodeWithLabel("List.get()"))));
        assertThat(aug, not(hasEdge(actionNodeWithLabel("Collection.add()"), CONDITION, actionNodeWithLabel("Collection.clear()"))));
        assertThat(aug, not(hasEdge(actionNodeWithLabel("List.get()"), CONDITION, actionNodeWithLabel("Collection.clear()"))));
    }
}
