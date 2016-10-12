package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static egroum.EGroumDataEdge.Type.ORDER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MissingElementViolationFactoryTest {
    @Test
    public void fullInstance() throws Exception {
        Instance instance = buildInstance(buildAUG().withActionNode(":action:")).withNode(":action:").build();

        ViolationFactory strategy = new MissingElementViolationFactory();
        assertFalse(strategy.isViolation(instance));
    }

    @Test
    public void missingNode() throws Exception {
        Instance instance = buildInstance(buildAUG().withActionNode(":action:")).build();

        MissingElementViolationFactory strategy = new MissingElementViolationFactory();
        assertTrue(strategy.isViolation(instance));
    }

    @Test
    public void missingEdge() throws Exception {
        final TestAUGBuilder builder = buildAUG().withActionNodes(":a1:", ":a2:").withDataEdge(":a1:", ORDER, ":a2:");
        Instance instance = buildInstance(builder).withNode(":a1:").withNode(":a2:").build();

        MissingElementViolationFactory strategy = new MissingElementViolationFactory();
        assertTrue(strategy.isViolation(instance));
    }
}
