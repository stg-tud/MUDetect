package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasParameterEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasSelectionEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUGForMethod;
import static org.junit.Assert.assertThat;

public class EncodeAssertTest {

    @Test
    public void encodesAssertLikeCheckWithThrow() {
        APIUsageExample aug = buildAUGForMethod("void m(java.util.List l) {" +
                "  assert l.isEmpty();" +
                "}");

        assertThat(aug, hasSelectionEdge(actionNodeWith(label("Collection.isEmpty()")), actionNodeWith(label("<throw>"))));
        assertThat(aug, hasParameterEdge(actionNodeWith(label("AssertionError.<init>")), actionNodeWith(label("<throw>"))));
    }

    @Test
    public void assertControlsSubsequentStatements() {
        APIUsageExample aug = buildAUGForMethod("void m(java.util.Iterator it) {" +
                "  assert it.hasNext();\n" +
                "  it.next();" +
                "}");

        assertThat(aug, hasSelectionEdge(actionNodeWith(label("Iterator.hasNext()")), actionNodeWith(label("Iterator.next()"))));
    }
}
