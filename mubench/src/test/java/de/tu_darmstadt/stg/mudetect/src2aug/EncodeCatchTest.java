package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.THROW;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeCatchTest {
    @Test
    public void encodesException() throws Exception {
        APIUsageExample aug = AUGBuilderTestUtils.buildAUG("class C {\n" +
                "  void m(java.util.List<String> l, Object obj) {\n" +
                "    try {\n" +
                "      l.contains(obj);\n" +
                "    } catch(java.lang.ClassCastException e) {\n" +
                "      l.clear();\n" +
                "    }\n" +
                "  }\n" +
                "}");
        
        assertThat(aug, hasHandleEdge(actionNodeWithLabel("ClassCastException.<catch>"), actionNodeWithLabel("List.clear()")));
        assertThat(aug, hasEdge(actionNodeWithLabel("List.contains()"), THROW, dataNodeWithLabel("ClassCastException")));
    }
}
