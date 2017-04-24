package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasSelEdge;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeConditionsImplicitTest {
    @Test
    public void guard() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) {\n" +
                "  if (l.isEmpty()) {\n" +
                "    return;\n" +
                "  }\n" +
                "  l.clear();\n" +
                "}");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("Collection.clear()")));
    }

    @Test
    public void nullGuard() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) {\n" +
                "  if (l == null) {\n" +
                "    return;\n" +
                "  }\n" +
                "  l.clear();\n" +
                "}");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("<r>"), actionNodeWithLabel("Collection.clear()")));
    }

    @Test
    public void sanitizer() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) {\n" +
                "  if (l.isEmpty()) {\n" +
                "    throw new Exception();\n" +
                "  }\n" +
                "  l.clear();\n" +
                "}");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("Collection.clear()")));
    }

    @Test
    public void loopGuardBreak() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) {\n" +
                "  for (Object o : l) {\n" +
                "    if (o == null) break;\n" +
                "    o.hashCode();\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("<r>"), actionNodeWithLabel("Object.hashCode()")));
    }

    @Test
    public void loopGuardContinue() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) {\n" +
                "  for (Object o : l) {\n" +
                "    if (o == null) continue;\n" +
                "    o.hashCode();\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("<r>"), actionNodeWithLabel("Object.hashCode()")));
    }

    @Test
    public void loopGuardAfterLoop() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) {\n" +
                "  for (Object o : l) {\n" +
                "    if (o == null) continue;\n" +
                "  }\n" +
                "  o.hashCode();\n" +
                "}");

        assertThat(aug, not(hasSelEdge(actionNodeWithLabel("<r>"), actionNodeWithLabel("Object.hashCode()"))));
    }
}
