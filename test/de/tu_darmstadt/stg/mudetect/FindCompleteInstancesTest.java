package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.AUGBuilder;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.AUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.InstanceTestUtils.hasInstance;
import static egroum.EGroumDataEdge.Type.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FindCompleteInstancesTest {
    @Test
    public void findsSingleNodeInstance() throws Exception {
        assertFindsInstance(buildAUG().withActionNode("C.m()"));
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

    private void assertFindsInstance(AUGBuilder builder) {
        AUG pattern = builder.build();
        System.out.println(pattern);
        AUG target = builder.build();

        List<Instance> instances = new GreedyInstanceFinder().findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(pattern));
    }
}
