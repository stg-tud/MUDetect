package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.junit.Test;

import java.util.Optional;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.SELECTION;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MissingAssignmentNoViolationPredicateTest {
    @Test
    public void considersMissingAssignmentNoViolation() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("C.m()", "=").withEdge("C.m()", SELECTION, "=");
        TestAUGBuilder target = buildAUG().withActionNode("C.m()");
        TestOverlapBuilder violation = buildOverlap(pattern, target).withNode("C.m()");

        Optional<Boolean> decision = new MissingAssignmentNoViolationPredicate().apply(violation.build());

        assertThat(decision, is(Optional.of(false)));
    }

    @Test
    public void considersMissingAssignmentWithParameterNoViolation() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("C.m()", "=").withDataNode("D")
                .withEdge("C.m()", SELECTION, "=").withEdge("D", PARAMETER, "=");
        TestAUGBuilder target = buildAUG().withActionNode("C.m()");
        TestOverlapBuilder violation = buildOverlap(pattern, target).withNode("C.m()");

        Optional<Boolean> decision = new MissingAssignmentNoViolationPredicate().apply(violation.build());

        assertThat(decision, is(Optional.of(false)));
    }

    @Test
    public void doesNotDecideAboutMissingAssignmentAndMissingEdge() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A.b()", "C.m()", "=")
                .withEdge("A.b()", ORDER, "C.m()").withEdge("C.m()", SELECTION, "=");
        TestAUGBuilder target = buildAUG().withActionNodes("A.b()", "C.m()");
        TestOverlapBuilder violation = buildOverlap(pattern, target).withNodes("A.b()", "C.m()");

        Optional<Boolean> decision = new MissingAssignmentNoViolationPredicate().apply(violation.build());

        assertThat(decision, is(Optional.empty()));
    }
}
