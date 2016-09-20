package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.AUGBuilder;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.AUGBuilder.*;
import static de.tu_darmstadt.stg.mudetect.model.InstanceTestUtils.*;
import static egroum.EGroumDataEdge.Type.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class FindPartialInstancesTest {
    @Test
    public void findsMissingMethod() throws Exception {
        AUGBuilder builder = buildAUG().withActionNode("C.m()");
        AUG target = builder.build();
        AUG pattern = builder.withActionNode("C.n()")
                .withDataEdge("C.m()", ORDER, "C.n()").build();

        assertFindsInstance(pattern, target, target);
    }

    @Test
    public void findsMissingConditionEquation() throws Exception {
        AUGBuilder builder = buildAUG().withActionNode("List.get()");
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
        AUGBuilder builder = buildAUG().withActionNode("A.size()").withDataNode("int")
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

    private void assertFindsInstance(AUG pattern, AUG target, AUG expectedInstance) {
        List<Instance> instances = new GreedyInstanceFinder().findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(expectedInstance));
    }
}
