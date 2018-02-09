package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.*;
import static org.junit.Assert.assertThat;

public class EncodeCastTest {
    @Test
    public void encodesCast() {
        APIUsageExample aug = buildAUG("class C {\n" +
                "  void m(Object obj) {\n" +
                "    java.util.List l = (java.util.List) obj;\n" +
                "    l.size();\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasNode(actionNodeWith(label("List.<cast>"))));
    }

    @Test 
    public void addsTransitiveParameterEdgeThroughCast() {
        AUGConfiguration conf = new AUGConfiguration(){{buildTransitiveDataEdges = true;}};
        APIUsageExample aug = buildAUG("class C {\n" +
                "  void m(java.util.List l) {\n" +
                "    A a = (A) l.get(0);\n" +
                "    l.remove(a);\n" +
                "  }\n" +
                "}",
                conf);

        assertThat(aug, hasParameterEdge(actionNodeWith(label("List.get()")), actionNodeWith(label("List.remove()"))));
    }

    @Test 
    public void addsTransitiveReceiverEdgeThroughCast() {
        AUGConfiguration conf = new AUGConfiguration(){{buildTransitiveDataEdges = true;}};
        APIUsageExample aug = buildAUG("class C {\n" +
                "  void m(java.util.List l) {\n" +
                "    A a = (A) l.get(0);\n" +
                "    a.m();\n" +
                "  }\n" +
                "}",
                conf);

        assertThat(aug, hasReceiverEdge(actionNodeWith(label("List.get()")), actionNodeWith(label("A.m()"))));
    }
}
