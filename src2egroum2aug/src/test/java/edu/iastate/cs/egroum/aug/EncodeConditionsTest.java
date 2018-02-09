package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.dataNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class EncodeConditionsTest {
    // Predicate condition: if (l.isEmpty())

    @Test
    public void addsNodeForConditionPredicate() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) { if (l.isEmpty()) l.get(0); }");

        assertThat(aug, hasNode(actionNodeWith(label("Collection.isEmpty()"))));
    }

    @Test
    public void addsSelEdgeFromPredicateToGuardedAction() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) { if (l.isEmpty()) l.get(0); }");

        assertThat(aug, hasSelectionEdge(actionNodeWith(label("Collection.isEmpty()")), actionNodeWith(label("List.get()"))));
    }

    @Test
    public void doesNotControlSubsequentAction() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  if (l.isEmpty())\n" +
                "    l.add(null);\n" +
                "  }\n" +
                "  l.clear();\n" +
                " }");

        assertThat(aug, not(hasSelectionEdge(actionNodeWith(label("Collection.isEmpty()")), actionNodeWith(label("Collection.clear()")))));
    }

    // State relation condition: if (l.size() > 42)

    @Test
    public void addNodesForConditionOperatorAndOperandsConnectedByParaEdges() {
        AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG("void m(java.util.List l) { if (l.size() > 42) l.get(41); }", conf);

        assertThat(aug, hasNodes(actionNodeWith(label("Collection.size()")), actionNodeWith(label("<r>")), dataNodeWith(label("int"))));
        if (conf.buildTransitiveDataEdges)
            assertThat(aug, hasParameterEdge(actionNodeWith(label("Collection.size()")), actionNodeWith(label("<r>"))));
        assertThat(aug, hasParameterEdge(dataNodeWith(label("int")), actionNodeWith(label("<r>"))));
    }

    @Test
    public void addsSelEdgeFromOperatorToGuardedAction() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) { if (l.size() > 42) l.get(41); }");

        assertThat(aug, hasSelectionEdge(actionNodeWith(label("<r>")), actionNodeWith(label("List.get()"))));
    }

    @Test
    public void addsSelEdgeFromActionOperandToGuardedAction() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) { if (l.size() > 42) l.get(41); }");

        assertThat(aug, hasSelectionEdge(actionNodeWith(label("Collection.size()")), actionNodeWith(label("List.get()"))));
    }

    @Test
    public void noSelEdgeFromLiteralOperandToGuardedCondition() {
        APIUsageExample aug = buildAUG("void m(java.util.List l) { if (l.size() > 42) l.get(41); }");

        assertThat(aug, not(hasSelectionEdge(actionNodeWith(label("int")), actionNodeWith(label("List.get()")))));
    }
}
