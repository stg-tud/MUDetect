package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import de.tu_darmstadt.stg.eko.mudetect.model.AUGBuilder;
import egroum.EGroumActionNode;
import egroum.EGroumNode;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.eko.mudetect.Instance.findInstances;
import static de.tu_darmstadt.stg.eko.mudetect.model.AUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.eko.mudetect.model.InstanceTestUtils.contains;
import static egroum.EGroumDataEdge.Type.CONDITION;
import static egroum.EGroumDataEdge.Type.ORDER;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class FindMissingNodesTest {
    @Test
    public void findsMissingMethod() throws Exception {
        AUGBuilder builder = buildAUG().withActionNode("C.m()");
        AUG target = builder.build();
        AUG pattern = builder.withActionNode("C.n()")
                .withDataEdge("C.m()", ORDER, "C.n()").build();

        List<Instance> violations = findInstances(target, pattern);

        assertThat(violations, hasSize(1));
        assertThat(violations.get(0), not(contains(builder.getNode("C.n()"))));
    }

    @Test
    public void findsMissingConditionEquation() throws Exception {
        AUGBuilder builder = buildAUG().withDataNode("int").withActionNodes("List.size()", "List.get()", ">")
                .withDataEdge("List.size()", PARAMETER, ">")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge(">", CONDITION, "List.get()");
        EGroumNode pattern_gt = builder.getNode(">");
        AUG pattern = builder.build();

        AUG target = buildAUG().withDataNode("int").withActionNodes("A.foo()", "List.get()", ">")
                .withDataEdge("A.foo()", PARAMETER, ">")
                .withDataEdge("int", PARAMETER, ">")
                .withDataEdge(">", CONDITION, "List.get()").build();

        List<Instance> instances = findInstances(target, pattern);

        System.out.println("instances = " + instances);
        assertThat(instances, hasSize(1));
        assertThat(instances.get(0), not(contains(pattern_gt)));
    }
}
