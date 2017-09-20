package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.DEFINITION;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.RECEIVER;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeLoopsTest {
    @Test
    public void addsRepeatEdge() throws Exception {
        APIUsageExample aug = AUGBuilderTestUtils.buildAUG("void m(java.util.Stack s) {" +
                "  while(!s.isEmpty()) {" +
                "    s.pop();" +
                "  }" +
                "}");

        assertThat(aug, hasRepeatEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("Stack.pop()")));
    }

    @Test
    public void loopDoesNotControlAfterLoop() throws Exception {
        APIUsageExample aug = AUGBuilderTestUtils.buildAUG("void m(java.util.Stack s) {\n" +
                "  while(!s.isEmpty()) {\n" +
                "    s.pop();\n" +
                "  }\n" +
                "  s.push(null);\n" +
                "}");

        assertThat(aug, not(hasRepeatEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("Stack.push()"))));
    }
    
    @Test
    public void encodesForeach() throws Exception {
        APIUsageExample aug = AUGBuilderTestUtils.buildAUG("void m(java.lang.Iterable it) {\n" +
                "  for (Object o : it) {\n" +
                "    o.hashCode();\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasEdge(actionNodeWithLabel("Iterable.iterator()"), DEFINITION, dataNodeWithLabel("Iterator")));
        assertThat(aug, hasEdge(dataNodeWithLabel("Iterator"), RECEIVER, actionNodeWithLabel("Iterator.hasNext()")));
        assertThat(aug, hasEdge(dataNodeWithLabel("Iterator"), RECEIVER, actionNodeWithLabel("Iterator.next()")));
        assertThat(aug, hasRepeatEdge(actionNodeWithLabel("Iterator.hasNext()"), actionNodeWithLabel("Iterator.next()")));
        assertThat(aug, hasRepeatEdge(actionNodeWithLabel("Iterator.hasNext()"), actionNodeWithLabel("Object.hashCode()")));
    }
}
