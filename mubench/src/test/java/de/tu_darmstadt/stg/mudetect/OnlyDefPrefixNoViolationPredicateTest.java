package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.junit.Test;

import java.util.Optional;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.DEFINITION;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.RECEIVER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.REPETITION;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static org.junit.Assert.assertEquals;

public class OnlyDefPrefixNoViolationPredicateTest {
    @Test
    public void simpleDefPrefixNoViolation() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("prod()", "use()").withDataNode("P")
                .withEdge("prod()", DEFINITION, "P").withEdge("P", RECEIVER, "use()");
        TestAUGBuilder target = buildAUG().withActionNodes("prod()").withDataNode("P")
                .withEdge("prod()", DEFINITION, "P");
        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("prod()", "P").withEdge("prod()", DEFINITION, "P");

        Optional<Boolean> decision = new OnlyDefPrefixNoViolationPredicate().apply(overlap.build());

        assertEquals(decision, Optional.of(false));
    }

    @Test
    public void onlyDefPrefixNoViolation() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("it()", "hN()", "n()").withDataNodes("Itrbl", "Iter")
                .withEdge("Itrbl", RECEIVER, "it()").withEdge("it()", DEFINITION, "Iter")
                .withEdge("Iter", RECEIVER, "hN()").withEdge("Iter", RECEIVER, "n()")
                .withEdge("it()", ORDER, "hN()").withEdge("it()", ORDER, "n()")
                .withEdge("it()", REPETITION, "n()").withEdge("hN()", REPETITION, "n()").withEdge("hN()", ORDER, "n()");
        TestAUGBuilder target = buildAUG().withActionNodes("it()").withDataNodes("Itrbl", "Iter")
                .withEdge("Itrbl", RECEIVER, "it()").withEdge("it()", DEFINITION, "Iter");
        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("Itrbl", "it()", "Iter").withEdge("Itrbl", RECEIVER, "it()")
                .withEdge("it()", DEFINITION, "Iter");

        Optional<Boolean> decision = new OnlyDefPrefixNoViolationPredicate().apply(overlap.build());

        assertEquals(decision, Optional.of(false));
    }
}
