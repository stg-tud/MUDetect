package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasSelectionEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
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

        assertThat(aug, hasSelectionEdge(NodeMatchers.actionNodeWith(label("Collection.isEmpty()")), NodeMatchers.actionNodeWith(label("Collection.clear()"))));
    }

    @Test
    public void nullGuard() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  if (l == null) {\n" +
                "    return;\n" +
                "  }\n" +
                "  l.clear();\n" +
                "}");

        assertThat(aug, hasSelectionEdge(NodeMatchers.actionNodeWith(label("<nullcheck>")), NodeMatchers.actionNodeWith(label("Collection.clear()"))));
    }

    @Test
    public void sanitizer() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  if (l.isEmpty()) {\n" +
                "    throw new Exception();\n" +
                "  }\n" +
                "  l.clear();\n" +
                "}");

        assertThat(aug, hasSelectionEdge(NodeMatchers.actionNodeWith(label("Collection.isEmpty()")), NodeMatchers.actionNodeWith(label("Collection.clear()"))));
    }

    @Test
    public void loopGuardBreak() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  for (Object o : l) {\n" +
                "    if (o == null) break;\n" +
                "    o.hashCode();\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasSelectionEdge(NodeMatchers.actionNodeWith(label("<nullcheck>")), NodeMatchers.actionNodeWith(label("Object.hashCode()"))));
    }

    @Test
    public void loopGuardContinue() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  for (Object o : l) {\n" +
                "    if (o == null) continue;\n" +
                "    o.hashCode();\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasSelectionEdge(NodeMatchers.actionNodeWith(label("<nullcheck>")), NodeMatchers.actionNodeWith(label("Object.hashCode()"))));
    }

    @Test
    public void loopGuardAfterLoop() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  for (Object o : l) {\n" +
                "    if (o == null) continue;\n" +
                "  }\n" +
                "  o.hashCode();\n" +
                "}");

        assertThat(aug, not(hasSelectionEdge(NodeMatchers.actionNodeWith(label("<r>")), NodeMatchers.actionNodeWith(label("Object.hashCode()")))));
    }

    @Test
    public void guardDefinition() {
        APIUsageExample aug = buildAUG("void m(Object o) { if (o == null) o = new Object(); o.hashCode();}");

        assertThat(aug, hasSelectionEdge(NodeMatchers.actionNodeWith(label("<nullcheck>")), NodeMatchers.actionNodeWith(label("Object.<init>"))));
    }

    @Test
    public void guardRedefinition() {
        APIUsageExample aug = buildAUG("void m(Object o) { if (o == null) { o = new Object(); o.hashCode();}}");

        assertThat(aug, hasSelectionEdge(NodeMatchers.actionNodeWith(label("<nullcheck>")), NodeMatchers.actionNodeWith(label("Object.<init>"))));
        assertThat(aug, hasSelectionEdge(NodeMatchers.actionNodeWith(label("<nullcheck>")), NodeMatchers.actionNodeWith(label("Object.hashCode()"))));
    }
}
