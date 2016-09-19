package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.AUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.InstanceTestUtils;
import egroum.EGroumNode;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.AUGBuilder.*;
import static de.tu_darmstadt.stg.mudetect.model.InstanceTestUtils.*;
import static egroum.EGroumDataEdge.Type.CONDITION;
import static egroum.EGroumDataEdge.Type.ORDER;
import static egroum.EGroumDataEdge.Type.PARAMETER;
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

        List<Instance> instances = new GreedyInstanceFinder().findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(target));
    }

    @Test
    public void findsMissingConditionEquation() throws Exception {
        AUGBuilder builder = buildAUG().withActionNode("List.get()");
        AUG expectedInstance = builder.build();

        AUG pattern = builder.withDataNode("int").withActionNodes("List.size()", ">")
                .withDataEdge("List.size()", PARAMETER, ">")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge(">", CONDITION, "List.get()").build();

        AUG target = buildAUG().withDataNode("int").withActionNodes("A.foo()", ">", "List.get()")
                .withDataEdge("A.foo()", PARAMETER, ">")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge(">", CONDITION, "List.get()").build();

        List<Instance> instances = new GreedyInstanceFinder().findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(expectedInstance));
    }
}
