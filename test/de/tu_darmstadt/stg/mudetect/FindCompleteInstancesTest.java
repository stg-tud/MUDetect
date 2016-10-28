package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.InstanceTestUtils.hasInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.extend;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
import static egroum.EGroumDataEdge.Type.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FindCompleteInstancesTest {
    @Test
    public void findsSingleNodeInstance() throws Exception {
        assertFindsInstance2(buildAUG().withActionNode("C.m()"));
    }

    @Test
    public void findsTwoNodeInstance() throws Exception {
        assertFindsInstance2(buildAUG().withActionNodes("C.a()", "C.b()").withDataEdge("C.a()", ORDER, "C.b()"));
    }

    @Test
    public void ignoresUnmappableTargetNode() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = extend(pattern).withDataNode("C").withDataEdge("A", ORDER, "C");

        assertFindsInstance2(pattern, target);
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

    private void assertFindsInstance(TestAUGBuilder builder) {
        Pattern pattern = somePattern(builder);
        AUG target = builder.build();

        List<Instance> instances = new GreedyInstanceFinder().findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(pattern));
    }

    private void assertFindsInstance2(TestAUGBuilder patternAndTargetBuilder) {
        assertFindsInstance2(patternAndTargetBuilder, patternAndTargetBuilder);
    }

    private void assertFindsInstance2(TestAUGBuilder patternBuilder, TestAUGBuilder targetBuilder) {
        Pattern pattern = somePattern(patternBuilder);
        AUG target = targetBuilder.build();

        List<Instance> instances = new AlternativeMappingsInstanceFinder().findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(pattern));
    }
}
