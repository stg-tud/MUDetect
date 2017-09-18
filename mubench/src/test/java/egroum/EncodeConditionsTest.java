package egroum;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class EncodeConditionsTest {
    // Predicate condition: if (l.isEmpty())

    @Test
    public void addsNodeForConditionPredicate() throws Exception {
        APIUsageExample aug = buildAUG("void m(java.util.List l) { if (l.isEmpty()) l.get(0); }");

        assertThat(aug, hasNode(actionNodeWithLabel("Collection.isEmpty()")));
    }

    @Test
    public void addsSelEdgeFromPredicateToGuardedAction() throws Exception {
        APIUsageExample aug = buildAUG("void m(java.util.List l) { if (l.isEmpty()) l.get(0); }");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("List.get()")));
    }

    @Test
    public void doesNotControlSubsequentAction() throws Exception {
        APIUsageExample aug = buildAUG("void m(java.util.List l) {\n" +
                "  if (l.isEmpty())\n" +
                "    l.add(null);\n" +
                "  }\n" +
                "  l.clear();\n" +
                " }");

        assertThat(aug, not(hasSelEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("Collection.clear()"))));
    }

    // State relation condition: if (l.size() > 42)

    @Test
    public void addNodesForConditionOperatorAndOperandsConnectedByParaEdges() throws Exception {
        AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG("void m(java.util.List l) { if (l.size() > 42) l.get(41); }", conf);

        assertThat(aug, hasNodes(actionNodeWithLabel("Collection.size()"), actionNodeWithLabel("<r>"), dataNodeWithLabel("int")));
        if (conf.buildTransitiveDataEdges)
        	assertThat(aug, hasEdge(actionNodeWithLabel("Collection.size()"), PARAMETER, actionNodeWithLabel("<r>")));
        assertThat(aug, hasEdge(dataNodeWithLabel("int"), PARAMETER, actionNodeWithLabel("<r>")));
    }

    @Test
    public void addsSelEdgeFromOperatorToGuardedAction() throws Exception {
        APIUsageExample aug = buildAUG("void m(java.util.List l) { if (l.size() > 42) l.get(41); }");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("<r>"), actionNodeWithLabel("List.get()")));
    }

    @Test
    public void addsSelEdgeFromActionOperandToGuardedAction() throws Exception {
        APIUsageExample aug = buildAUG("void m(java.util.List l) { if (l.size() > 42) l.get(41); }");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("Collection.size()"), actionNodeWithLabel("List.get()")));
    }

    @Test
    public void noSelEdgeFromLiteralOperandToGuardedCondition() throws Exception {
        APIUsageExample aug = buildAUG("void m(java.util.List l) { if (l.size() > 42) l.get(41); }");

        assertThat(aug, not(hasSelEdge(actionNodeWithLabel("int"), actionNodeWithLabel("List.get()"))));
    }
}
