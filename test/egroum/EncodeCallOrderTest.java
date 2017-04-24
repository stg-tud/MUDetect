package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Ignore;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.dataNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasEdge;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumDataEdge.Type.ORDER;
import static org.junit.Assert.assertThat;

public class EncodeCallOrderTest {
    @Test @Ignore
    public void encodesTransitiveOrderEdges() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) {\n" +
                "  l.add(null);\n" +
                "  l.get(0);\n" +
                "  l.clear();\n" +
                "}");

        assertThat(aug, hasEdge(dataNodeWithLabel("Collection.add()"), ORDER, actionNodeWithLabel("List.get()")));
        assertThat(aug, hasEdge(dataNodeWithLabel("Collection.add()"), ORDER, actionNodeWithLabel("Collection.clear()")));
        assertThat(aug, hasEdge(dataNodeWithLabel("List.get()"), ORDER, actionNodeWithLabel("Collection.clear()")));
    }
}
