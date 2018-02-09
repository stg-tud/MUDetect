package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasOrderEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasThrowEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeCallOrderTest {
    @Test
    public void encodesTransitiveOrderEdges() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  l.add(null);\n" +
                "  l.get(0);\n" +
                "  l.clear();\n" +
                "}");

        assertThat(aug, hasOrderEdge(NodeMatchers.actionNodeWith(label("Collection.add()")), NodeMatchers.actionNodeWith(label("List.get()"))));
        assertThat(aug, hasOrderEdge(NodeMatchers.actionNodeWith(label("Collection.add()")), NodeMatchers.actionNodeWith(label("Collection.clear()"))));
        assertThat(aug, hasOrderEdge(NodeMatchers.actionNodeWith(label("List.get()")), NodeMatchers.actionNodeWith(label("Collection.clear()"))));
        assertThat(aug, not(hasThrowEdge(NodeMatchers.actionNodeWith(label("Collection.add()")), NodeMatchers.actionNodeWith(label("List.get()")))));
        assertThat(aug, not(hasThrowEdge(NodeMatchers.actionNodeWith(label("Collection.add()")), NodeMatchers.actionNodeWith(label("Collection.clear()")))));
        assertThat(aug, not(hasThrowEdge(NodeMatchers.actionNodeWith(label("List.get()")), NodeMatchers.actionNodeWith(label("Collection.clear()")))));
    }
}
