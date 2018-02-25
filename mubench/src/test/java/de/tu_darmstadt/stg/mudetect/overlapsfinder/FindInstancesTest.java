package de.tu_darmstadt.stg.mudetect.overlapsfinder;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.*;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.SELECTION;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.instance;
import static de.tu_darmstadt.stg.mudetect.overlapsfinder.OverlapsFinderTestUtils.assertFindsOverlaps;
import static de.tu_darmstadt.stg.mudetect.overlapsfinder.OverlapsFinderTestUtils.findOverlaps;
import static de.tu_darmstadt.stg.mudetect.utils.CollectionUtils.only;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FindInstancesTest {
    @Test
    public void findsSingleNodeInstance() {
        assertFindsInstance(buildAUG().withActionNode("C.m()"));
    }

    @Test
    public void findsTwoNodeInstance() {
        assertFindsInstance(buildAUG().withActionNodes("C.a()", "C.b()").withEdge("C.a()", SELECTION, "C.b()"));
    }

    @Test
    public void findsThreeNodeChain() {
        assertFindsInstance(buildAUG().withActionNodes("A", "B", "C")
                .withEdge("A", SELECTION, "B").withEdge("B", SELECTION, "C"));
    }

    @Test
    public void findsFourNodeChain() {
        assertFindsInstance(buildAUG().withActionNodes("A", "B", "C", "D")
                .withEdge("A", SELECTION, "B").withEdge("B", SELECTION, "C").withEdge("C", SELECTION, "D"));
    }

    @Test
    public void ignoresUnmappableTargetNode() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withEdge("A", SELECTION, "B");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withEdge("A", SELECTION, "B")
                .withDataNode("C").withEdge("A", SELECTION, "C");

        TestOverlapBuilder instance = buildOverlap(pattern, target).withNodes("A", "B").withEdge("A", SELECTION, "B");

        assertFindsOverlaps(pattern, target, instance);
    }

    @Test
    public void findsTwoOverlappingInstances() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withEdge("A", SELECTION, "B");
        TestAUGBuilder target = buildAUG().withActionNode("A").withActionNode("B1", "B").withActionNode("B2", "B")
                .withEdge("A", SELECTION, "B1").withEdge("A", SELECTION, "B2");

        TestOverlapBuilder instance1 = buildOverlap(pattern, target).withNode("A", "A")
                .withNode("B1", "B").withEdge("A", "A", SELECTION, "B1", "B");
        TestOverlapBuilder instance2 = buildOverlap(pattern, target).withNode("A", "A")
                .withNode("B2", "B").withEdge("A", "A", SELECTION, "B2", "B");
        assertFindsOverlaps(pattern, target, instance1, instance2);
    }

    @Test
    public void findsCallReceiver() {
        assertFindsInstance(buildAUG().withDataNode("C").withActionNode("C.m()").withEdge("C", RECEIVER, "C.m()"));
    }

    @Test
    public void findsMultipleCalls() {
        assertFindsInstance(buildAUG().withDataNode("C").withActionNodes("C.m()", "C.n()")
                .withEdge("C", RECEIVER, "C.m()")
                .withEdge("C", RECEIVER, "C.n()"));
    }

    @Test
    public void findCallArguments() {
        assertFindsInstance(buildAUG().withDataNode("Object").withActionNode("Object.equals()")
                .withEdge("Object", PARAMETER, "Object.equals()"));
    }

    @Test
    public void findsMultipleEdgesBetweenTwoNodes() {
        assertFindsInstance(buildAUG().withActionNodes("A.m()", "A.n()")
                .withEdge("A.m()", ORDER, "A.n()")
                .withEdge("A.m()", PARAMETER, "A.n()"));
    }

    @Test
    public void findsConditionPredicate() {
        assertFindsInstance(buildAUG().withActionNodes("A.predicate()", "B.m()")
                .withEdge("A.predicate()", SELECTION, "B.m()"));
    }

    @Test
    public void findsConditionEquation() {
        assertFindsInstance(buildAUG().withDataNode("int").withActionNodes("List.size()", "List.get()", ">")
                .withEdge("List.size()", PARAMETER, ">")
                .withEdge("int", PARAMETER, ">")
                .withEdge(">", SELECTION, "List.get()"));
    }

    @Test
    public void findsResultAsArgument() {
        assertFindsInstance(buildAUG().withActionNodes("A.getX()", "B.takeX()")
                .withEdge("A.getX()", PARAMETER, "B.takeX()"));
    }

    @Test
    public void findsExceptionHandling() {
        assertFindsInstance(buildAUG().withActionNodes("C.throws()", "E.handler()")
                .withDataNode("SomeException")
                .withEdge("C.throws()", THROW, "SomeException")
                .withEdge("SomeException", SELECTION, "E.handler()")
                .withEdge("SomeException", PARAMETER, "E.handler()"));
    }

    @Test
    public void findsThrow() {
        assertFindsInstance(buildAUG().withActionNodes("throw", "SomeException.<init>")
                .withEdge("SomeException.<init>", PARAMETER, "throw"));
    }

    @Test
    public void findsFinally() {
        assertFindsInstance(buildAUG().withActionNodes("C.throws()", "A.cleanup()")
                .withEdge("C.throws()", FINALLY, "A.cleanup()"));
    }

    @Test
    public void findsLargestAlternative() {
        // Both pattern and target are equal. However, to find this the algorithm needs to map the edges correctly,
        // because both branches start with the same call, but one has an additional call afterwards.
        assertFindsInstance(buildAUG().withActionNodes("A.check()", "C.foo()")
                .withActionNode("B1", "B.op()")
                .withActionNode("B2", "B.op()")
                .withEdge("B1", SELECTION, "C.foo()")
                .withEdge("A.check()", SELECTION, "B1")
                .withEdge("A.check()", SELECTION, "B2"));
    }

    /**
     * With the first greedy instance finder we had the problem that with too many alternatives (like when many equal
     * nodes appear in a pattern/target) the finder would likely pick a wrong mapping between pattern and target nodes,
     * causing many false positives. This test is derived from an evaluation scenario where this became apparent.
     */
    @Test
    public void issue_unluckyMapping() {
        TestAUGBuilder pattern = buildAUG()
                .withActionNode("init", "StringBuilder.<init>")
                .withActionNode("toString", "Object.toString()")
                .withActionNode("append1", "StringBuilder.append()")
                .withActionNode("append2", "StringBuilder.append()")
                .withEdge("init", RECEIVER, "append1")
                .withEdge("init", RECEIVER, "append2")
                .withEdge("init", RECEIVER, "toString")
                .withEdge("append1", ORDER, "append2")
                .withEdge("append1", ORDER, "toString")
                .withEdge("append2", ORDER, "toString");

        TestAUGBuilder target = buildAUG()
                .withActionNode("init", "StringBuilder.<init>")
                .withActionNode("toString", "Object.toString()")
                .withActionNode("append1", "StringBuilder.append()")
                .withActionNode("append2", "StringBuilder.append()")
                // Adding the same edges in different order to increase the likelihood of picking a wrong mapping.
                .withEdge("append2", ORDER, "toString")
                .withEdge("init", RECEIVER, "append2")
                .withEdge("init", RECEIVER, "toString")
                .withEdge("init", RECEIVER, "append1")
                .withEdge("append1", ORDER, "toString")
                .withEdge("append1", ORDER, "append2");

        List<Overlap> overlaps = findOverlaps(target, pattern);

        assertThat(only(overlaps).getEdgeSize(), is(6));
    }

    @Test
    public void handlesMultipleEdgesBetweenTwoNodes() {
        assertFindsInstance(buildAUG().withActionNodes("A", "B")
                .withEdge("A", RECEIVER, "B")
                .withEdge("A", SELECTION, "B"));
    }

    private void assertFindsInstance(TestAUGBuilder patternAndTargetBuilder) {
        assertFindsOverlaps(patternAndTargetBuilder, patternAndTargetBuilder, instance(patternAndTargetBuilder));
    }
}
