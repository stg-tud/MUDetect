package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.junit.Test;

import java.util.Optional;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.*;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MissingCatchNoViolationPredicateTest {
    @Test
    public void missingCatchNoViolation() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("mayFail()", "<catch>").withDataNode("SomeException")
                .withEdge("mayFail()", THROW, "SomeException")
                .withEdge("SomeException", PARAMETER, "<catch>");
        TestAUGBuilder target = buildAUG().withActionNodes("mayFail()");
        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("mayFail()");

        Optional<Boolean> decision = new MissingCatchNoViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.of(false)));
    }

    @Test
    public void missingThrowNoViolation() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("mayFail()").withDataNode("SomeException")
                .withEdge("mayFail()", THROW, "SomeException");
        TestAUGBuilder target = buildAUG().withActionNodes("mayFail()");
        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("mayFail()");

        Optional<Boolean> decision = new MissingCatchNoViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.of(false)));
    }
}
