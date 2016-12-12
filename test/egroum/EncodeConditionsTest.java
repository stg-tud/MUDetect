package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class EncodeConditionsTest {
    // Predicate condition: if (l.isEmpty())

    @Test
    public void addsNodeForConditionPredicate() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) { if (l.isEmpty()) l.get(0); }");

        assertThat(aug, hasNode(actionNodeWithLabel("Collection.isEmpty()")));
    }

    @Test
    public void addsSelEdgeFromPredicateToGuardedAction() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) { if (l.isEmpty()) l.get(0); }");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("List.get()")));
    }

    // Negated-predicate condition: if (!l.isEmpty())

    @Test
    public void addsNodeForConditionNegatedPredicate() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) { if (!l.isEmpty()) l.get(0); }");

        assertThat(aug, hasNode(actionNodeWithLabel("Collection.isEmpty()")));
        if (!EGroumBuilder.REMOVE_UNARY_OPERATORS)
        	assertThat(aug, hasNode(actionNodeWithLabel("!")));
        if (!EGroumBuilder.REMOVE_UNARY_OPERATORS)
        	assertThat(aug, hasEdge(actionNodeWithLabel("Collection.isEmpty()"), PARAMETER, actionNodeWithLabel("!")));
    }

    @Test
    public void addsSelEdgeFromNegationOperatorToGuardedAction() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) { if (!l.isEmpty()) l.get(0); }");

        if (!EGroumBuilder.REMOVE_UNARY_OPERATORS)
        	assertThat(aug, hasSelEdge(actionNodeWithLabel("!"), actionNodeWithLabel("List.get()")));
    }

    @Test
    public void addsSelEdgeFromNegatedPredicteToGuardedAction() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) { if (!l.isEmpty()) l.get(0); }");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("Collection.isEmpty()"), actionNodeWithLabel("List.get()")));
    }

    // State relation condition: if (l.size() > 42)

    @Test
    public void addNodesForConditionOperatorAndOperandsConnectedByParaEdges() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) { if (l.size() > 42) l.get(41); }");

        assertThat(aug, hasNodes(actionNodeWithLabel("Collection.size()"), actionNodeWithLabel("<r>"), dataNodeWithLabel("int")));
        assertThat(aug, hasEdge(actionNodeWithLabel("Collection.size()"), PARAMETER, actionNodeWithLabel("<r>")));
        assertThat(aug, hasEdge(dataNodeWithLabel("int"), PARAMETER, actionNodeWithLabel("<r>")));
    }

    @Test
    public void addsSelEdgeFromOperatorToGuardedAction() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) { if (l.size() > 42) l.get(41); }");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("<r>"), actionNodeWithLabel("List.get()")));
    }

    @Test
    public void addsSelEdgeFromActionOperandToGuardedAction() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) { if (l.size() > 42) l.get(41); }");

        assertThat(aug, hasSelEdge(actionNodeWithLabel("Collection.size()"), actionNodeWithLabel("List.get()")));
    }

    @Test
    public void noSelEdgeFromLiteralOperandToGuardedCondition() throws Exception {
        AUG aug = buildAUG("void m(java.util.List l) { if (l.size() > 42) l.get(41); }");

        assertThat(aug, not(hasSelEdge(actionNodeWithLabel("int"), actionNodeWithLabel("List.get()"))));
    }
}
