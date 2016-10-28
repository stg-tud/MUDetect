package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.*;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.InstanceTestUtils.hasInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.extend;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
import static egroum.EGroumDataEdge.Type.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FindPartialInstancesTest {
    @Test
    public void findsMissingNode() throws Exception {
        TestAUGBuilder target = buildAUG().withActionNode("C.m()");
        TestAUGBuilder pattern = buildAUG().withActionNode("C.m()")
                .withActionNode("C.n()").withDataEdge("C.m()", ORDER, "C.n()");

        TestInstanceBuilder instance = buildInstance(target, pattern).withNode("C.m()");
        assertFindsInstance2(pattern, target, instance);
    }

    @Test
    public void excludesNonEqualNode() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNode("A").withActionNode("B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNode("A").withActionNode("C").withDataEdge("A", ORDER, "C");

        TestInstanceBuilder instance = buildInstance(target, pattern).withNode("A");
        assertFindsInstance2(pattern, target, instance);
    }

    @Test
    public void ignoresNonEqualEdge() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withDataEdge("A", PARAMETER, "B");

        TestInstanceBuilder instance1 = buildInstance(target, pattern).withNode("A");
        TestInstanceBuilder instance2 = buildInstance(target, pattern).withNode("B");
        assertFindsInstance2(pattern, target, instance1, instance2);
    }

    @Test
    public void ignoresReverseEdge() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withDataEdge("B", ORDER, "A");

        TestInstanceBuilder instance1 = buildInstance(target, pattern).withNode("A");
        TestInstanceBuilder instance2 = buildInstance(target, pattern).withNode("B");
        assertFindsInstance2(pattern, target, instance1, instance2);
    }

    @Test
    public void findsMissingConditionEquation() throws Exception {
        TestAUGBuilder builder = buildAUG().withActionNode("List.get()");
        AUG expectedInstance = builder.build();

        AUG pattern = extend(builder).withDataNode("int").withActionNodes("List.size()", ">")
                .withDataEdge("List.size()", PARAMETER, ">")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge(">", CONDITION, "List.get()").build();

        AUG target = extend(builder).withDataNode("int").withActionNodes("A.foo()", ">")
                .withDataEdge("A.foo()", PARAMETER, ">")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge(">", CONDITION, "List.get()").build();

        assertFindsInstance(pattern, target, expectedInstance);
    }

    @Test
    public void excludesConditionWithPrimitiveOverlap() throws Exception {
        TestAUGBuilder builder = buildAUG().withActionNode("A.size()").withDataNode("int")
                .withDataEdge("A.size()", DEFINITION, "int");
        AUG expectedInstance = builder.build();

        AUG pattern = extend(builder).withActionNodes("B.size()", ">")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge("B.size()", PARAMETER, ">").build();

        AUG target = extend(builder).withActionNodes("C.foo()", ">")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge("C.foo()", PARAMETER, ">").build();

        assertFindsInstance(pattern, target, expectedInstance);
    }

    @Test @Ignore("check why this does not work anymore!")
    public void findsOnlyOneInstance() throws Exception {
        final TestAUGBuilder builder = buildAUG().withActionNode("a1", "a").withActionNode("a2", "a")
                .withActionNode("b").withDataEdge("a1", ORDER, "a2").withDataEdge("a1", ORDER, "b");

        final AUG pattern = extend(builder).withDataEdge("b", ORDER, "a2").build();
        final AUG target = extend(builder).withDataEdge("a2", ORDER, "b").build();

        assertFindsInstance(pattern, target, builder.build());
    }

    private void assertFindsInstance(AUG patternAUG, AUG target, AUG expectedInstance) {
        List<Instance> instances = new GreedyInstanceFinder().findInstances(target, somePattern(patternAUG));

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(expectedInstance));
    }

    private void assertFindsInstance2(TestAUGBuilder patternBuilder,
                                      TestAUGBuilder targetBuilder,
                                      TestInstanceBuilder... expectedInstanceBuilder) {
        AUG target = targetBuilder.build();
        Pattern pattern = somePattern(patternBuilder.build());

        List<Instance> instances = new AlternativeMappingsInstanceFinder().findInstances(target, pattern);

        assertThat(instances, hasSize(expectedInstanceBuilder.length));
        Instance[] expectedInstances = new Instance[expectedInstanceBuilder.length];
        for (int i = 0; i < expectedInstanceBuilder.length; i++) {
            expectedInstances[i] = expectedInstanceBuilder[i].build();
        }

        assertThat(instances, containsInAnyOrder(expectedInstances));
    }
}
