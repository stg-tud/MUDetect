package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.matchers.AUGNodeMatchers;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGNodeMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.hasEdge;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.RECEIVER;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeCastTest {
    @Test
    public void encodesCast() {
        APIUsageExample aug = AUGBuilderTestUtils.buildAUG("class C {\n" +
                "  void m(Object obj) {\n" +
                "    java.util.List l = (java.util.List) obj;\n" +
                "    l.size();\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasNode(actionNodeWithLabel("List.<cast>")));
    }

    @Test 
    public void addsTransitiveParameterEdgeThroughCast() {
        AUGConfiguration conf = new AUGConfiguration(){{buildTransitiveDataEdges = true;}};
        APIUsageExample aug = AUGBuilderTestUtils.buildAUG("class C {\n" +
                "  void m(java.util.List l) {\n" +
                "    A a = (A) l.get(0);\n" +
                "    l.remove(a);\n" +
                "  }\n" +
                "}",
                conf);

        assertThat(aug, hasEdge(actionNodeWithLabel("List.get()"), PARAMETER, actionNodeWithLabel("List.remove()")));
    }

    @Test 
    public void addsTransitiveReceiverEdgeThroughCast() {
        AUGConfiguration conf = new AUGConfiguration(){{buildTransitiveDataEdges = true;}};
        APIUsageExample aug = AUGBuilderTestUtils.buildAUG("class C {\n" +
                "  void m(java.util.List l) {\n" +
                "    A a = (A) l.get(0);\n" +
                "    a.m();\n" +
                "  }\n" +
                "}",
                conf);

        assertThat(aug, hasEdge(actionNodeWithLabel("List.get()"), RECEIVER, actionNodeWithLabel("A.m()")));
    }
}
