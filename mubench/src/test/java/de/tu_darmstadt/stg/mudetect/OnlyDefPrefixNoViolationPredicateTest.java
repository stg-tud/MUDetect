package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.DEFINITION;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.RECEIVER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.REPETITION;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

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

    @Test
    public void missingNonDefPrefixIsNoDecision() {
        TestOverlapBuilder overlap = buildOverlap(buildAUG(), buildAUG().withActionNode("m()"));

        Optional<Boolean> decision = new OnlyDefPrefixNoViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.empty()));
    }

    @Test
    public void moreThanDefPrefixIsNoDecision() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("create()", "use()", "use2()").withDataNode("Object")
                .withEdge("create()", DEFINITION, "Object")
                .withEdge("Object", RECEIVER, "use()")
                .withEdge("Object", RECEIVER, "use2()");
        TestAUGBuilder target = buildAUG().withActionNodes("create()", "use()").withDataNode("Object")
                .withEdge("Object", RECEIVER, "use()")
                .withEdge("Object", RECEIVER, "use()");
        TestOverlapBuilder overlap = buildOverlap(target, pattern).withNodes("create()", "use()", "Object")
                .withEdge("Object", RECEIVER, "use()")
                .withEdge("Object", RECEIVER, "use()");

        Optional<Boolean> decision = new OnlyDefPrefixNoViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.empty()));
    }

    @Test @Ignore("This property might be reasonable, but it's not easy to implement it, hence, we didn't for now.")
    public void onlyDefPrefixWithPredecessorIsNoViolation() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("create()", "use()").withDataNodes("Creator", "Object")
                .withEdge("Creator", RECEIVER, "create()")
                .withEdge("create()", DEFINITION, "Object")
                .withEdge("Object", RECEIVER, "use()");
        TestAUGBuilder target = buildAUG().withActionNodes("create()").withDataNodes("Creator", "Object")
                .withEdge("Creator", RECEIVER, "create()")
                .withEdge("create()", DEFINITION, "Object");
        TestOverlapBuilder overlap = buildOverlap(target, pattern).withNodes("Creator", "create()", "Object")
                .withEdge("Creator", RECEIVER, "create()")
                .withEdge("create()", DEFINITION, "Object");

        Optional<Boolean> decision = new OnlyDefPrefixNoViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.of(false)));
    }

    @Test @Ignore("This property might be reasonable, but it's not easy to implement it, hence, we didn't for now.")
    public void onlyDefPrefixWithAdditionalEdgesIsNoViolation() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("iterator()", "hasNext()", "next()").withDataNode("Iterator")
                .withEdge("iterator()", DEFINITION, "Iterator")
                .withEdge("Iterator", RECEIVER, "hasNext()")
                .withEdge("Iterator", RECEIVER, "next()")
                .withEdge("iterator()", REPETITION, "next()")
                .withEdge("hasNext()", REPETITION, "next()");
        TestAUGBuilder target = buildAUG().withActionNodes("iterator()").withDataNode("Iterator")
                .withEdge("iterator()", DEFINITION, "Iterator");
        TestOverlapBuilder overlap = buildOverlap(target, pattern).withNodes("iterator()", "Iterator")
                .withEdge("iterator()", DEFINITION, "Iterator");

        Optional<Boolean> decision = new OnlyDefPrefixNoViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.of(false)));
    }
}
