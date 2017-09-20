package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasSelEdge;
import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeConditionsImplicitTest {
    @Test
    public void guard() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  if (l.isEmpty()) {\n" +
                "    return;\n" +
                "  }\n" +
                "  l.clear();\n" +
                "}");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("Collection.clear()")));
    }

    @Test
    public void nullGuard() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  if (l == null) {\n" +
                "    return;\n" +
                "  }\n" +
                "  l.clear();\n" +
                "}");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("<nullcheck>"), actionNodeWithLabel("Collection.clear()")));
    }

    @Test
    public void sanitizer() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  if (l.isEmpty()) {\n" +
                "    throw new Exception();\n" +
                "  }\n" +
                "  l.clear();\n" +
                "}");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("Collection.clear()")));
    }

    @Test
    public void loopGuardBreak() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  for (Object o : l) {\n" +
                "    if (o == null) break;\n" +
                "    o.hashCode();\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("<nullcheck>"), actionNodeWithLabel("Object.hashCode()")));
    }

    @Test
    public void loopGuardContinue() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  for (Object o : l) {\n" +
                "    if (o == null) continue;\n" +
                "    o.hashCode();\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("<nullcheck>"), actionNodeWithLabel("Object.hashCode()")));
    }

    @Test
    public void loopGuardAfterLoop() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  for (Object o : l) {\n" +
                "    if (o == null) continue;\n" +
                "  }\n" +
                "  o.hashCode();\n" +
                "}");

        assertThat(aug, not(hasSelEdge(actionNodeWithLabel("<r>"), actionNodeWithLabel("Object.hashCode()"))));
    }

    @Test
    public void guardDefinition() {
        APIUsageExample aug = buildAUG("void m(Object o) { if (o == null) o = new Object(); o.hashCode();}");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("<nullcheck>"), actionNodeWithLabel("Object.<init>")));
    }

    @Test
    public void guardRedefinition() {
        APIUsageExample aug = buildAUG("void m(Object o) { if (o == null) { o = new Object(); o.hashCode();}}");
        
        assertThat(aug, hasSelEdge(actionNodeWithLabel("<nullcheck>"), actionNodeWithLabel("Object.<init>")));
        assertThat(aug, hasSelEdge(actionNodeWithLabel("<nullcheck>"), actionNodeWithLabel("Object.hashCode()")));
    }
}
