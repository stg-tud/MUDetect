package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import java.util.HashSet;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static egroum.EGroumDataEdge.Type.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MissingElementViolationFactoryTest {
    @Test
    public void fullInstance() throws Exception {
        AUG patternAUG = buildAUG().withActionNode(":action:").build();
        Instance instance = new Instance(patternAUG, patternAUG.vertexSet(), patternAUG.edgeSet());

        ViolationFactory strategy = new MissingElementViolationFactory();
        assertFalse(strategy.isViolation(instance));
    }

    @Test
    public void missingNode() throws Exception {
        AUG patternAUG = buildAUG().withActionNode(":action:").build();
        Instance instance = new Instance(patternAUG, new HashSet<>(), new HashSet<>());

        MissingElementViolationFactory strategy = new MissingElementViolationFactory();
        assertTrue(strategy.isViolation(instance));
    }

    @Test
    public void missingEdge() throws Exception {
        AUG patternAUG = buildAUG().withActionNodes(":a1:", ":a2:").withDataEdge(":a1:", ORDER, ":a2:").build();
        Instance instance = new Instance(patternAUG, patternAUG.vertexSet(), new HashSet<>());

        MissingElementViolationFactory strategy = new MissingElementViolationFactory();
        assertTrue(strategy.isViolation(instance));
    }
}
