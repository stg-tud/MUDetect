package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.fullInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
import static egroum.EGroumDataEdge.Type.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static utils.CollectionUtils.only;

public class FindCompleteInstancesTest {
    @Test
    public void findsSingleNodeInstance() throws Exception {
        assertFindsInstance(buildAUG().withActionNode("C.m()"));
    }

    @Test
    public void findsTwoNodeInstance() throws Exception {
        assertFindsInstance(buildAUG().withActionNodes("C.a()", "C.b()").withDataEdge("C.a()", ORDER, "C.b()"));
    }

    @Test
    public void findsThreeNodeChain() throws Exception {
        assertFindsInstance(buildAUG().withActionNodes("A", "B", "C")
                .withDataEdge("A", ORDER, "B").withDataEdge("B", ORDER, "C"));
    }

    @Test
    public void findsFourNodeChain() throws Exception {
        assertFindsInstance(buildAUG().withActionNodes("A", "B", "C", "D")
                .withDataEdge("A", ORDER, "B").withDataEdge("B", ORDER, "C").withDataEdge("C", ORDER, "D"));
    }

    @Test
    public void ignoresUnmappableTargetNode() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B")
                .withDataNode("C").withDataEdge("A", ORDER, "C");

        Instance instance = buildInstance(target, pattern).withNodes("A", "B").withEdge("A", ORDER, "B").build();
        assertFindsInstance(pattern, target, instance);
    }

    @Test
    public void findsTwoOverlappingInstances() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNode("A").withActionNode("B1", "B").withActionNode("B2", "B")
                .withDataEdge("A", ORDER, "B1").withDataEdge("A", ORDER, "B2");

        Instance instance1 = buildInstance(target, pattern).withNode("A", "A")
                .withNode("B1", "B").withEdge("A", "A", ORDER, "B1", "B").build();
        Instance instance2 = buildInstance(target, pattern).withNode("A", "A")
                .withNode("B2", "B").withEdge("A", "A", ORDER, "B2", "B").build();
        assertFindsInstance(pattern, target, instance1, instance2);
    }

    @Test
    public void findsCallReceiver() throws Exception {
        assertFindsInstance(buildAUG().withDataNode("C").withActionNode("C.m()").withDataEdge("C", RECEIVER, "C.m()"));
    }

    @Test
    public void findsMultipleCalls() throws Exception {
        assertFindsInstance(buildAUG().withDataNode("C").withActionNodes("C.m()", "C.n()")
                .withDataEdge("C", RECEIVER, "C.m()")
                .withDataEdge("C", RECEIVER, "C.n()"));
    }

    @Test
    public void findCallArguments() throws Exception {
        assertFindsInstance(buildAUG().withDataNode("Object").withActionNode("Object.equals()")
                .withDataEdge("Object", PARAMETER, "Object.equals()"));
    }

    @Test
    public void findsMultipleEdgesBetweenTwoNodes() throws Exception {
        assertFindsInstance(buildAUG().withActionNodes("A.m()", "A.n()")
                .withDataEdge("A.m()", ORDER, "A.n()")
                .withDataEdge("A.m()", PARAMETER, "A.n()"));
    }

    @Test
    public void findsConditionPredicate() throws Exception {
        assertFindsInstance(buildAUG().withActionNodes("A.predicate()", "B.m()")
                .withDataEdge("A.predicate()", CONDITION, "B.m()"));
    }

    @Test
    public void findsConditionEquation() throws Exception {
        assertFindsInstance(buildAUG().withDataNode("int").withActionNodes("List.size()", "List.get()", ">")
                .withDataEdge("List.size()", PARAMETER, ">")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge(">", CONDITION, "List.get()"));
    }

    @Test
    public void findsResultAsArgument() throws Exception {
        assertFindsInstance(buildAUG().withActionNodes("A.getX()", "B.takeX()")
                .withDataEdge("A.getX()", PARAMETER, "B.takeX()"));
    }

    @Test
    public void findsExceptionHandling() throws Exception {
        assertFindsInstance(buildAUG().withActionNodes("C.throws()", "E.handler()")
                .withDataNode("SomeException")
                .withDataEdge("C.throws()", THROW, "SomeException")
                .withDataEdge("SomeException", CONDITION, "E.handler()")
                .withDataEdge("SomeException", PARAMETER, "E.handler()"));
    }

    @Test
    public void findsThrow() throws Exception {
        assertFindsInstance(buildAUG().withActionNodes("throw", "SomeException.<init>")
                .withDataEdge("SomeException.<init>", PARAMETER, "throw"));
    }

    @Test
    public void findsFinally() throws Exception {
        assertFindsInstance(buildAUG().withActionNodes("C.throws()", "A.cleanup()")
                .withDataEdge("C.throws()", FINALLY, "A.cleanup()"));
    }

    @Test
    public void findsLargestAlternative() throws Exception {
        // Both pattern and target are equal. However, to find this the algorithm needs to map the edges correctly,
        // because both branches start with the same call, but one has an additional call afterwards.
        assertFindsInstance(buildAUG().withActionNodes("A.check()", "C.foo()")
                .withActionNode("B1", "B.op()")
                .withActionNode("B2", "B.op()")
                .withDataEdge("B1", ORDER, "C.foo()")
                .withDataEdge("A.check()", CONDITION, "B1")
                .withDataEdge("A.check()", CONDITION, "B2"));
    }

    /**
     * With the first greedy instance finder we had the problem that with too many alternatives (like when many equal
     * nodes appear in a pattern/target) the finder would likely pick a wrong mapping between pattern and target nodes,
     * causing many false positives. This test is derived from an evaluation scenario where this became apparent.
     */
    @Test
    public void issue_unluckyMapping() throws Exception {
        TestAUGBuilder pattern = buildAUG()
                .withActionNode("init", "StringBuilder.<init>")
                .withActionNode("toString", "Object.toString()")
                .withActionNode("append1", "StringBuilder.append()")
                .withActionNode("append2", "StringBuilder.append()")
                .withDataEdge("init", RECEIVER, "append1")
                .withDataEdge("init", RECEIVER, "append2")
                .withDataEdge("init", RECEIVER, "toString")
                .withDataEdge("append1", ORDER, "append2")
                .withDataEdge("append1", ORDER, "toString")
                .withDataEdge("append2", ORDER, "toString");

        TestAUGBuilder target = buildAUG()
                .withActionNode("init", "StringBuilder.<init>")
                .withActionNode("toString", "Object.toString()")
                .withActionNode("append1", "StringBuilder.append()")
                .withActionNode("append2", "StringBuilder.append()")
                // Adding the same edges in different order to increase the likelihood of picking a wrong mapping.
                .withDataEdge("append2", ORDER, "toString")
                .withDataEdge("init", RECEIVER, "append2")
                .withDataEdge("init", RECEIVER, "toString")
                .withDataEdge("init", RECEIVER, "append1")
                .withDataEdge("append1", ORDER, "toString")
                .withDataEdge("append1", ORDER, "append2");

        List<Instance> instances = findInstances(target, pattern);

        assertThat(only(instances).getEdgeSize(), is(6));
    }

    private void assertFindsInstance(TestAUGBuilder patternAndTargetBuilder) {
        assertFindsInstance(patternAndTargetBuilder, patternAndTargetBuilder, fullInstance(patternAndTargetBuilder));
    }

    private void assertFindsInstance(TestAUGBuilder patternBuilder, TestAUGBuilder targetBuilder,
                                     Instance... expectedInstances) {
        List<Instance> instances = findInstances(patternBuilder, targetBuilder);

        assertThat(instances, hasSize(expectedInstances.length));
        assertThat(instances, containsInAnyOrder(expectedInstances));
    }

    private List<Instance> findInstances(TestAUGBuilder patternBuilder, TestAUGBuilder targetBuilder) {
        Pattern pattern = somePattern(patternBuilder);
        AUG target = targetBuilder.build();

        return new AlternativeMappingsInstanceFinder().findInstances(target, pattern);
    }
}
