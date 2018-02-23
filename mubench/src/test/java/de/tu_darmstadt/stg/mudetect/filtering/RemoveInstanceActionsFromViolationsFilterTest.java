package de.tu_darmstadt.stg.mudetect.filtering;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.QUALIFIER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.RECEIVER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.SELECTION;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RemoveInstanceActionsFromViolationsFilterTest {
    @Test
    public void removesInstanceAction() {
        TestAUGBuilder target = buildAUG().withActionNodes("m()", "n()").withEdge("m()", SELECTION, "n()");
        Overlap instance = buildOverlap(target).withNode("m()").build();
        Overlap violation = buildOverlap(target).withNodes("m()", "n()").withEdge("m()", SELECTION, "n()").build();

        Overlap reducedViolation = violation.without(instance);

        Overlap expectedReducedOverlap = buildOverlap(target).withNodes("n()").build();
        assertTrue(expectedReducedOverlap.isSameTargetOverlap(reducedViolation));
        assertTrue(expectedReducedOverlap.isSamePatternOverlap(reducedViolation));
    }

    @Test
    public void removesIsolatedDataNode() {
        TestAUGBuilder target = buildAUG().withActionNode("m()").withDataNode("O").withEdge("O", RECEIVER, "m()");
        Overlap instance = buildOverlap(target).withNode("m()").build();
        Overlap violation = buildOverlap(target).withNodes("O", "m()").withEdge("O", RECEIVER, "m()").build();

        Overlap reducedViolation = violation.without(instance);

        assertThat(reducedViolation.getMappedTargetNodes(), is(empty()));
    }

    @Test
    public void removesIsolatedDataNodeRecursive() {
        TestAUGBuilder target = buildAUG().withActionNode("m()").withDataNodes("O", "P")
                .withEdge("P", QUALIFIER, "O").withEdge("O", RECEIVER, "m()");
        Overlap instance = buildOverlap(target).withNode("m()").build();
        Overlap violation = buildOverlap(target).withNodes("P", "O", "m()")
                .withEdge("P", QUALIFIER, "O").withEdge("O", RECEIVER, "m()").build();

        Overlap reducedViolation = violation.without(instance);

        assertThat(reducedViolation.getMappedTargetNodes(), is(empty()));
    }
}
