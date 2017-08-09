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

    @Test @Ignore("We don't currently add transitive edges at all. Edges 'around' casts may help to match pre-generics" +
            "code with post-generics code, but I don't really think there's much need for this.")
    public void addsTransitiveParameterEdgeThroughCast() throws Exception {
        AUG aug = buildAUG("class C {\n" +
                "  void m(java.util.List l) {\n" +
                "    A a = (A) l.get(0);\n" +
                "    l.remove(a);\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasEdge(actionNodeWithLabel("List.get()"), PARAMETER, actionNodeWithLabel("List.remove()")));
    }

    @Test @Ignore("We don't currently add transitive edges at all. Generally, the cast in this example is strictly" +
            "required (modulo generics), since otherwise we couldn't call A.m().")
    public void addsTransitiveReceiverEdgeThroughCast() throws Exception {
        AUG aug = buildAUG("class C {\n" +
                "  void m(java.util.List l) {\n" +
                "    A a = (A) l.get(0);\n" +
                "    a.m();\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasEdge(actionNodeWithLabel("List.get()"), RECEIVER, actionNodeWithLabel("A.m()")));
    }
}
