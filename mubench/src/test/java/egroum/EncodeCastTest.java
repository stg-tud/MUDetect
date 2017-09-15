package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Ignore;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static egroum.EGroumDataEdge.Type.RECEIVER;
import static org.junit.Assert.assertThat;

public class EncodeCastTest {
    @Test
    public void encodesCast() throws Exception {
        AUG aug = buildAUG("class C {\n" +
                "  void m(Object obj) {\n" +
                "    java.util.List l = (java.util.List) obj;\n" +
                "    l.size();\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasNode(actionNodeWithLabel("List.<cast>")));
    }

    @Test 
    public void addsTransitiveParameterEdgeThroughCast() throws Exception {
    	AUGConfiguration conf = new AUGConfiguration(){{buildTransitiveDataEdges = true;}};
        AUG aug = buildAUG("class C {\n" +
                "  void m(java.util.List l) {\n" +
                "    A a = (A) l.get(0);\n" +
                "    l.remove(a);\n" +
                "  }\n" +
                "}",
                conf);

        assertThat(aug, hasEdge(actionNodeWithLabel("List.get()"), PARAMETER, actionNodeWithLabel("List.remove()")));
    }

    @Test 
    public void addsTransitiveReceiverEdgeThroughCast() throws Exception {
    	AUGConfiguration conf = new AUGConfiguration(){{buildTransitiveDataEdges = true;}};
        AUG aug = buildAUG("class C {\n" +
                "  void m(java.util.List l) {\n" +
                "    A a = (A) l.get(0);\n" +
                "    a.m();\n" +
                "  }\n" +
                "}",
                conf);

        assertThat(aug, hasEdge(actionNodeWithLabel("List.get()"), RECEIVER, actionNodeWithLabel("A.m()")));
    }
}
