package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasRepeatEdge;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeLoopsTest {
    @Test
    public void addsRepeatEdge() throws Exception {
        AUG aug = buildAUG("void m(java.util.Stack s) {" +
                "  while(!s.isEmpty()) {" +
                "    s.pop();" +
                "  }" +
                "}");

        assertThat(aug, hasRepeatEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("Stack.pop()")));
    }
}
