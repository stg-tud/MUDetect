package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.hamcrest.Matchers;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.hasEdge;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.CONDITION;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.ORDER;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeCallOrderTest {
    @Test
    public void encodesTransitiveOrderEdges() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  l.add(null);\n" +
                "  l.get(0);\n" +
                "  l.clear();\n" +
                "}");
        
        assertThat(aug, hasEdge(actionNodeWithLabel("Collection.add()"), ORDER, actionNodeWithLabel("List.get()")));
        assertThat(aug, hasEdge(actionNodeWithLabel("Collection.add()"), ORDER, actionNodeWithLabel("Collection.clear()")));
        assertThat(aug, hasEdge(actionNodeWithLabel("List.get()"), ORDER, actionNodeWithLabel("Collection.clear()")));
        assertThat(aug, Matchers.not(hasEdge(actionNodeWithLabel("Collection.add()"), CONDITION, actionNodeWithLabel("List.get()"))));
        assertThat(aug, Matchers.not(hasEdge(actionNodeWithLabel("Collection.add()"), CONDITION, actionNodeWithLabel("Collection.clear()"))));
        assertThat(aug, Matchers.not(hasEdge(actionNodeWithLabel("List.get()"), CONDITION, actionNodeWithLabel("Collection.clear()"))));
    }
}
