package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasEdge;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.notHaveEdge;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumDataEdge.Type.*;
import static org.junit.Assert.assertThat;

public class EncodeCallOrderTest {
    @Test
    public void encodesTransitiveOrderEdges() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) {\n" +
                "  l.add(null);\n" +
                "  l.get(0);\n" +
                "  l.clear();\n" +
                "}");
        
        assertThat(aug, hasEdge(actionNodeWithLabel("Collection.add()"), ORDER, actionNodeWithLabel("List.get()")));
        assertThat(aug, hasEdge(actionNodeWithLabel("Collection.add()"), ORDER, actionNodeWithLabel("Collection.clear()")));
        assertThat(aug, hasEdge(actionNodeWithLabel("List.get()"), ORDER, actionNodeWithLabel("Collection.clear()")));
        assertThat(aug, notHaveEdge(actionNodeWithLabel("Collection.add()"), CONDITION, actionNodeWithLabel("List.get()")));
        assertThat(aug, notHaveEdge(actionNodeWithLabel("Collection.add()"), CONDITION, actionNodeWithLabel("Collection.clear()")));
        assertThat(aug, notHaveEdge(actionNodeWithLabel("List.get()"), CONDITION, actionNodeWithLabel("Collection.clear()")));
    }
}
