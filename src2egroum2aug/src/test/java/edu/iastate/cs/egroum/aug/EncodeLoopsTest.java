package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.dataNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeLoopsTest {
    @Test
    public void addsRepeatEdge() {
        APIUsageExample aug = buildAUG("void m(java.util.Stack s) {" +
                "  while(!s.isEmpty()) {" +
                "    s.pop();" +
                "  }" +
                "}");

        assertThat(aug, hasRepeatEdge(actionNodeWith(label("Collection.isEmpty()")), actionNodeWith(label("Stack.pop()"))));
    }

    @Test
    public void loopDoesNotControlAfterLoop() {
        APIUsageExample aug = buildAUG("void m(java.util.Stack s) {\n" +
                "  while(!s.isEmpty()) {\n" +
                "    s.pop();\n" +
                "  }\n" +
                "  s.push(null);\n" +
                "}");

        assertThat(aug, not(hasRepeatEdge(actionNodeWith(label("Collection.isEmpty()")), actionNodeWith(label("Stack.push()")))));
    }
    
    @Test
    public void encodesForeach() {
        APIUsageExample aug = buildAUG("void m(java.lang.Iterable it) {\n" +
                "  for (Object o : it) {\n" +
                "    o.hashCode();\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasDefinitionEdge(actionNodeWith(label("Iterable.iterator()")), dataNodeWith(label("Iterator"))));
        assertThat(aug, hasReceiverEdge(dataNodeWith(label("Iterator")), actionNodeWith(label("Iterator.hasNext()"))));
        assertThat(aug, hasReceiverEdge(dataNodeWith(label("Iterator")), actionNodeWith(label("Iterator.next()"))));
        assertThat(aug, hasRepeatEdge(actionNodeWith(label("Iterator.hasNext()")), actionNodeWith(label("Iterator.next()"))));
        assertThat(aug, hasRepeatEdge(actionNodeWith(label("Iterator.hasNext()")), actionNodeWith(label("Object.hashCode()"))));
    }
}
