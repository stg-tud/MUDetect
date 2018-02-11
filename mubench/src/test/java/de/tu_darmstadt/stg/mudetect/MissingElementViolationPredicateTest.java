package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MissingElementViolationPredicateTest {
    @Test
    public void fullInstance() {
        Overlap overlap = buildOverlap(buildAUG().withActionNode(":action:")).withNode(":action:").build();

        ViolationPredicate strategy = new MissingElementViolationPredicate();
        assertFalse(strategy.apply(overlap).isPresent());
    }

    @Test
    public void missingNode() {
        Overlap overlap = buildOverlap(buildAUG().withActionNode(":action:")).build();

        MissingElementViolationPredicate strategy = new MissingElementViolationPredicate();
        assertTrue(strategy.apply(overlap).orElse(false));
    }

    @Test
    public void missingEdge() {
        final TestAUGBuilder builder = buildAUG().withActionNodes(":a1:", ":a2:").withEdge(":a1:", ORDER, ":a2:");
        Overlap overlap = buildOverlap(builder).withNode(":a1:").withNode(":a2:").build();

        MissingElementViolationPredicate strategy = new MissingElementViolationPredicate();
        assertTrue(strategy.apply(overlap).orElse(false));
    }
}
