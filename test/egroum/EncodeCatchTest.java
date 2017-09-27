package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;

import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumDataEdge.Type.THROW;
import static egroum.EGroumDataEdge.Type.CONDITION;
import static org.junit.Assert.assertThat;

public class EncodeCatchTest {
    @Test
    public void encodesException() throws Exception {
        AUG aug = buildAUG("class C {\n" +
                "  void m(java.util.List<String> l, Object obj) {\n" +
                "    try {\n" +
                "      l.contains(obj);\n" +
                "    } catch(java.lang.ClassCastException e) {\n" +
                "      l.clear();\n" +
                "    }\n" +
                "  }\n" +
                "}");
        
        assertThat(aug, hasEdge(actionNodeWithLabel("List.contains()"), THROW, dataNodeWithLabel("ClassCastException")));
        assertThat(aug, hasEdge(actionNodeWithLabel("ClassCastException.<catch>"), CONDITION, actionNodeWithLabel("List.clear()")));
    }
}
