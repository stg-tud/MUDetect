package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.InstanceTestUtils.hasInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.extend;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
import static egroum.EGroumDataEdge.Type.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FindPartialInstancesTest {
    @Test
    public void findsMissingNode() throws Exception {
        TestAUGBuilder target = buildAUG().withActionNode("C.m()");
        TestAUGBuilder pattern = extend(target).withActionNode("C.n()").withDataEdge("C.m()", ORDER, "C.n()");

        assertFindsInstance2(pattern, target, target);
    }

    @Test
    public void excludesNonEqualNode() throws Exception {
        TestAUGBuilder expectation = buildAUG().withActionNode("A");
        TestAUGBuilder pattern = extend(expectation).withActionNode("B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = extend(expectation).withActionNode("C").withDataEdge("A", ORDER, "C");

        assertFindsInstance2(pattern, target, expectation);
    }

    @Test
    public void ignoresNonEqualEdge() throws Exception {
        TestAUGBuilder expectation1 = buildAUG().withActionNode("A");
        TestAUGBuilder expectation2 = buildAUG().withActionNode("B");
        TestAUGBuilder pattern = extend(expectation1, expectation2).withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = extend(expectation1, expectation2).withDataEdge("A", PARAMETER, "B");

        assertFindsInstance2(pattern, target, expectation1, expectation2);
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
                                      TestAUGBuilder... expectedInstanceBuilder) {
        AUG target = targetBuilder.build();
        Pattern pattern = somePattern(patternBuilder.build());

        List<Instance> instances = new AlternativeMappingsInstanceFinder().findInstances(target, pattern);

        assertThat(instances, hasSize(expectedInstanceBuilder.length));
        for (TestAUGBuilder expectationBuilder : expectedInstanceBuilder) {
            assertThat(instances, hasInstance(expectationBuilder.build()));
        }
    }
}
