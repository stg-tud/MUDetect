package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.junit.Test;

import java.util.Optional;

import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.REPETITION;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.*;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MissingDefPrefixNoViolationPredicateTest {
    @Test
    public void missingNonDefPrefixIsNoDecision() {
        TestOverlapBuilder overlap = buildOverlap(buildAUG().withActionNode("m()"), buildAUG());

        Optional<Boolean> decision = new MissingDefPrefixNoViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.empty()));
    }

    @Test
    public void missingDefPrefixAndMoreIsNoDecision() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("create()", "use()", "use2()").withDataNode("Object")
                .withEdge("create()", DEFINITION, "Object")
                .withEdge("Object", RECEIVER, "use()").withEdge("create()", RECEIVER, "use()")
                .withEdge("Object", RECEIVER, "use2()").withEdge("create()", RECEIVER, "use2()");
        TestAUGBuilder target = buildAUG().withActionNodes("use()").withDataNode("Object")
                .withEdge("Object", RECEIVER, "use()");
        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("use()", "Object")
                .withEdge("Object", RECEIVER, "use()");

        Optional<Boolean> decision = new MissingDefPrefixNoViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.empty()));
    }

    @Test
    public void missingDefPrefixAndEdgeBetweenTwoMappedNodesIsNoDecision() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("create()", "use()", "use2()").withDataNode("Object")
                .withEdge("create()", DEFINITION, "Object")
                .withEdge("Object", RECEIVER, "use()").withEdge("create()", RECEIVER, "use()")
                .withEdge("Object", RECEIVER, "use2()").withEdge("create()", RECEIVER, "use2()")
                .withEdge("use()", ORDER, "use2()");
        TestAUGBuilder target = buildAUG().withActionNodes("use()", "use2()").withDataNode("Object")
                .withEdge("Object", RECEIVER, "use()")
                .withEdge("Object", RECEIVER, "use2()");
        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("use()", "use2()", "Object")
                .withEdge("Object", RECEIVER, "use()")
                .withEdge("Object", RECEIVER, "use2()");

        Optional<Boolean> decision = new MissingDefPrefixNoViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.empty()));
    }

    @Test
    public void missingDefPrefixIsNoViolation() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("create()", "use()").withDataNode("Object")
                .withEdge("create()", DEFINITION, "Object")
                .withEdge("Object", RECEIVER, "use()").withEdge("create()", RECEIVER, "use()");
        TestAUGBuilder target = buildAUG().withActionNodes("use()").withDataNode("Object")
                .withEdge("Object", RECEIVER, "use()");
        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("use()", "Object")
                .withEdge("Object", RECEIVER, "use()");

        Optional<Boolean> decision = new MissingDefPrefixNoViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.of(false)));
    }

    @Test
    public void missingDefPrefixWithPredecessorIsNoViolation() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("create()", "use()").withDataNodes("Creator", "Object")
                .withEdge("Creator", RECEIVER, "create()")
                .withEdge("create()", DEFINITION, "Object")
                .withEdge("Object", RECEIVER, "use()").withEdge("create()", RECEIVER, "use()");
        TestAUGBuilder target = buildAUG().withActionNodes("use()").withDataNodes("Object")
                .withEdge("Object", RECEIVER, "use()");
        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("use()", "Object")
                .withEdge("Object", RECEIVER, "use()");

        Optional<Boolean> decision = new MissingDefPrefixNoViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.of(false)));
    }

    @Test
    public void missingDefPrefixWithConditionEdgeIsNoViolation() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("iterator()", "hasNext()", "next()").withDataNode("Iterator")
                .withEdge("iterator()", DEFINITION, "Iterator")
                .withEdge("Iterator", RECEIVER, "hasNext()")
                .withEdge("Iterator", RECEIVER, "next()")
                .withEdge("iterator()", REPETITION, "next()")
                .withEdge("hasNext()", REPETITION, "next()");
        TestAUGBuilder target = buildAUG().withActionNodes("hasNext()", "next()").withDataNode("Iterator")
                .withEdge("Iterator", RECEIVER, "hasNext()")
                .withEdge("Iterator", RECEIVER, "next()")
                .withEdge("hasNext()", REPETITION, "next()");
        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("hasNext()", "next()", "Iterator")
                .withEdge("Iterator", RECEIVER, "hasNext()")
                .withEdge("Iterator", RECEIVER, "next()")
                .withEdge("hasNext()", CONDITION, "next()");

        Optional<Boolean> decision = new MissingDefPrefixNoViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.of(false)));
    }

    /**
     * In the following example, the usage of i2 might be mapped together with the iterator() call that creates i1,
     * because there is a rep edges from the i1 usage to the i2 usage. In this case only the def edge is missing.
     * However, this is still only a missing def prefix for the i2 usage.
     * <pre>
     *     Iterator i1 = iterator();
     *     while (i1.hasNext()) {
     *       Iterator i2 = i1.next();
     *       while(i2.hasNext()) {
     *         i2.next();
     *       }
     *     }
     * </pre>
     */
    @Test
    public void missingDefPrefixWithConditionEdgeToOtherDefinitionIsNoViolation() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("iterator()", "hasNext()", "next()").withDataNode("Iterator")
                .withEdge("iterator()", DEFINITION, "Iterator")
                .withEdge("Iterator", RECEIVER, "hasNext()")
                .withEdge("Iterator", RECEIVER, "next()")
                .withEdge("iterator()", REPETITION, "next()")
                .withEdge("hasNext()", REPETITION, "next()");
        TestAUGBuilder target = buildAUG().withActionNodes("iterator()", "hasNext()", "next()").withDataNode("Iterator")
                .withEdge("Iterator", RECEIVER, "hasNext()")
                .withEdge("Iterator", RECEIVER, "next()")
                .withEdge("iterator()", REPETITION, "next()")
                .withEdge("hasNext()", REPETITION, "next()");
        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("iterator()", "hasNext()", "next()", "Iterator")
                .withEdge("Iterator", RECEIVER, "hasNext()")
                .withEdge("Iterator", RECEIVER, "next()")
                .withEdge("iterator()", CONDITION, "next()")
                .withEdge("hasNext()", CONDITION, "next()");

        Optional<Boolean> decision = new MissingDefPrefixNoViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.of(false)));
    }
}
