package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.*;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
import static egroum.EGroumDataEdge.Type.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static utils.CollectionUtils.only;

public class FindPartialInstancesTest {
    @Test
    public void findsMissingNode() throws Exception {
        TestAUGBuilder target = buildAUG().withActionNode("C.m()");
        TestAUGBuilder pattern = buildAUG().withActionNode("C.m()")
                .withActionNode("C.n()").withDataEdge("C.m()", ORDER, "C.n()");

        TestInstanceBuilder instance = buildInstance(target, pattern).withNode("C.m()");
        assertFindsInstance(pattern, target, instance);
    }

    @Test
    public void excludesNonEqualNode() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNode("A").withActionNode("B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNode("A").withActionNode("C").withDataEdge("A", ORDER, "C");

        TestInstanceBuilder instance = buildInstance(target, pattern).withNode("A");
        assertFindsInstance(pattern, target, instance);
    }

    @Test
    public void ignoresNonEqualEdge() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withDataEdge("A", PARAMETER, "B");

        TestInstanceBuilder instance1 = buildInstance(target, pattern).withNode("A");
        TestInstanceBuilder instance2 = buildInstance(target, pattern).withNode("B");
        assertFindsInstance(pattern, target, instance1, instance2);
    }

    @Test
    public void ignoresReverseEdge() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withDataEdge("B", ORDER, "A");

        TestInstanceBuilder instance1 = buildInstance(target, pattern).withNode("A");
        TestInstanceBuilder instance2 = buildInstance(target, pattern).withNode("B");
        assertFindsInstance(pattern, target, instance1, instance2);
    }

    @Test
    public void mapsTargetEdgeOnlyOnce() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNode("A").withActionNode("B1", "B").withActionNode("B2", "B")
                .withDataEdge("A", ORDER, "B1").withDataEdge("A", ORDER, "B2");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");

        // TODO check why this doesn't work when using findInstance(TestAUGBuilder, TestAUGBuilder)
        List<Instance> instances = new AlternativeMappingsInstanceFinder().findInstances(target.build(), somePattern(pattern));

        assertThat(only(instances).getNodeSize(), is(2));
    }

    @Test
    public void findsInstanceAndPartialInstance() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B", "C")
                .withDataEdge("A", ORDER, "B").withDataEdge("B", ORDER, "C");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "C").withActionNode("B1", "B").withActionNode("B2", "B")
                .withDataEdge("A", ORDER, "B1").withDataEdge("A", ORDER, "B2").withDataEdge("B1", ORDER, "C");

        TestInstanceBuilder fullInstance = buildInstance(target, pattern).withNodes("A", "C").withNode("B1", "B")
                .withEdge("A", "A", ORDER, "B1", "B").withEdge("B1", "B", ORDER, "C", "C");
        TestInstanceBuilder partialInstance = buildInstance(target, pattern).withNode("A").withNode("B2", "B")
                .withEdge("A", "A", ORDER, "B2", "B");

        assertFindsInstance(pattern, target, fullInstance, partialInstance);
    }

    @Test @Ignore("discuss whether we want to include or exclude conditions this way")
    public void findsMissingConditionEquation() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("List.get()", "List.size()", ">").withDataNode("int")
                .withDataEdge("List.size()", PARAMETER, ">")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge(">", CONDITION, "List.get()");

        TestAUGBuilder target = buildAUG().withActionNodes("List.get()", "A.foo()", ">").withDataNode("int")
                .withDataEdge("A.foo()", PARAMETER, ">")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge(">", CONDITION, "List.get()");

        TestInstanceBuilder instance = buildInstance(target, pattern).withNode("List.get()");

        assertFindsInstance(pattern, target, instance);
    }

    @Test @Ignore("discuss whether we want to include or exclude conditions this way")
    public void excludesConditionWithPrimitiveOverlap() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A.size()", "B.size()", ">").withDataNode("int")
                .withDataEdge("A.size()", DEFINITION, "int")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge("B.size()", PARAMETER, ">");

        TestAUGBuilder target = buildAUG().withActionNodes("A.size()", "C.foo()", ">").withDataNode("int")
                .withDataEdge("A.size()", DEFINITION, "int")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge("C.foo()", PARAMETER, ">");

        TestInstanceBuilder instance = buildInstance(target, pattern)
                .withNodes("A.size()", "int").withEdge("A.size()", DEFINITION, "int");

        assertFindsInstance(pattern, target, instance);
    }

    @Test
    public void findsOnlyOneInstance() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNode("a1", "a").withActionNode("a2", "a").withActionNode("b")
                .withDataEdge("a1", ORDER, "a2").withDataEdge("a1", ORDER, "b").withDataEdge("b", ORDER, "a2");
        TestAUGBuilder target = buildAUG().withActionNode("a1", "a").withActionNode("a2", "a").withActionNode("b")
                .withDataEdge("a1", ORDER, "a2").withDataEdge("a1", ORDER, "b").withDataEdge("a2", ORDER, "b");

        List<Instance> instances = findInstances(pattern, target);

        assertThat(instances, hasSize(1));
    }

    private void assertFindsInstance(TestAUGBuilder patternBuilder,
                                     TestAUGBuilder targetBuilder,
                                     TestInstanceBuilder... expectedInstanceBuilder) {
        List<Instance> instances = findInstances(patternBuilder, targetBuilder);

        assertThat(instances, hasSize(expectedInstanceBuilder.length));
        Instance[] expectedInstances = new Instance[expectedInstanceBuilder.length];
        for (int i = 0; i < expectedInstanceBuilder.length; i++) {
            expectedInstances[i] = expectedInstanceBuilder[i].build();
        }

        assertThat(instances, containsInAnyOrder(expectedInstances));
    }

    private List<Instance> findInstances(TestAUGBuilder patternBuilder, TestAUGBuilder targetBuilder) {
        AUG target = targetBuilder.build();
        Pattern pattern = somePattern(patternBuilder.build());

        return new AlternativeMappingsInstanceFinder().findInstances(target, pattern);
    }
}
