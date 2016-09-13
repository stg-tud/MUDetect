package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.eko.mudetect.model.AUGBuilder.newAUG;
import static de.tu_darmstadt.stg.eko.mudetect.model.InstanceTestUtils.hasInstance;
import static egroum.EGroumDataEdge.Type.CONDITION;
import static egroum.EGroumDataEdge.Type.RECEIVER;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FindInstancesTest {
    @Test
    public void findsSingleNodeInstance() throws Exception {
        AUG pattern = newAUG().withActionNode("C.m()").build();
        AUG target = newAUG().withActionNode("C.m()").build();

        List<Instance> instances = InstanceFinder.findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(pattern));
    }

    @Test
    public void findsCallReceiver() throws Exception {
        AUG pattern = newAUG().withDataNode("C").withActionNode("C.m()").withDataEdge("C", RECEIVER, "C.m()").build();

        AUG target = newAUG().withDataNode("C").withActionNode("C.m()").withDataEdge("C", RECEIVER, "C.m()").build();

        List<Instance> instances = InstanceFinder.findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(pattern));
    }

    @Test
    public void findsMultipleCalls() throws Exception {
        AUG pattern = newAUG().withDataNode("C").withActionNodes("C.m()", "C.n()")
                .withDataEdge("C", RECEIVER, "C.m()")
                .withDataEdge("C", RECEIVER, "C.n()").build();

        AUG target = newAUG().withDataNode("C").withActionNodes("C.m()", "C.n()")
                .withDataEdge("C", RECEIVER, "C.m()")
                .withDataEdge("C", RECEIVER, "C.n()").build();

        List<Instance> instances = InstanceFinder.findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(pattern));
    }
}
