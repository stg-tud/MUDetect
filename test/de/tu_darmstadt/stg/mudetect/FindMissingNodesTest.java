package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.AUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.InstanceTestUtils;
import egroum.EGroumNode;
import org.junit.Test;

import java.util.List;

import static egroum.EGroumDataEdge.Type.CONDITION;
import static egroum.EGroumDataEdge.Type.ORDER;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class FindMissingNodesTest {
    @Test
    public void findsMissingMethod() throws Exception {
        AUGBuilder builder = AUGBuilder.buildAUG().withActionNode("C.m()");
        AUG target = builder.build();
        AUG pattern = builder.withActionNode("C.n()")
                .withDataEdge("C.m()", ORDER, "C.n()").build();

        List<Instance> violations = new GreedyInstanceFinder().findInstances(target, pattern);

        assertThat(violations, hasSize(1));
        assertThat(violations.get(0), not(InstanceTestUtils.contains(builder.getNode("C.n()"))));
    }

    @Test
    public void findsMissingConditionEquation() throws Exception {
        AUGBuilder builder = AUGBuilder.buildAUG().withDataNode("int").withActionNodes("List.size()", "List.get()", ">")
                .withDataEdge("List.size()", PARAMETER, ">")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge(">", CONDITION, "List.get()");
        EGroumNode pattern_gt = builder.getNode(">");
        AUG pattern = builder.build();

        AUG target = AUGBuilder.buildAUG().withDataNode("int").withActionNodes("A.foo()", "List.get()", ">")
                .withDataEdge("A.foo()", PARAMETER, ">")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge(">", CONDITION, "List.get()").build();

        List<Instance> instances = new GreedyInstanceFinder().findInstances(target, pattern);

        System.out.println("instances = " + instances);
        assertThat(instances, hasSize(1));
        assertThat(instances.get(0), not(InstanceTestUtils.contains(pattern_gt)));
    }
}
