package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import egroum.EGroumActionNode;
import egroum.EGroumDataNode;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.eko.mudetect.model.AUGBuilder.newAUG;
import static de.tu_darmstadt.stg.eko.mudetect.model.InstanceTestUtils.hasInstance;
import static egroum.EGroumDataEdge.Type.RECEIVER;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FindInstancesTest {
    @Test
    public void findsSingleNodeInstance() throws Exception {
        AUG pattern = newAUG(new EGroumActionNode("C.m()")).build();
        AUG target = newAUG(new EGroumActionNode("C.m()")).build();

        List<Instance> instances = InstanceFinder.findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(pattern));
    }

    @Test
    public void findsCallReceiver() throws Exception {
        EGroumDataNode pattern_C = new EGroumDataNode("C");
        EGroumActionNode pattern_m = new EGroumActionNode("C.m()");
        AUG pattern = newAUG(pattern_C, pattern_m).withDataEdge(pattern_C, RECEIVER, pattern_m).build();

        EGroumDataNode target_C = new EGroumDataNode("C");
        EGroumActionNode target_m = new EGroumActionNode("C.m()");
        AUG target = newAUG(target_C, target_m).withDataEdge(target_C, RECEIVER, target_m).build();

        List<Instance> instances = InstanceFinder.findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(pattern));
    }

    @Test
    public void findsMultipleCalls() throws Exception {
        EGroumDataNode pattern_C = new EGroumDataNode("C");
        EGroumActionNode pattern_m = new EGroumActionNode("C.m()");
        EGroumActionNode pattern_n = new EGroumActionNode("C.n()");
        AUG pattern = newAUG(pattern_C, pattern_m, pattern_n)
                .withDataEdge(pattern_C, RECEIVER, pattern_m)
                .withDataEdge(pattern_C, RECEIVER, pattern_n).build();

        EGroumDataNode target_C = new EGroumDataNode("C");
        EGroumActionNode target_m = new EGroumActionNode("C.m()");
        EGroumActionNode target_n = new EGroumActionNode("C.n()");
        AUG target = newAUG(target_C, target_m, target_n)
                .withDataEdge(target_C, RECEIVER, target_m)
                .withDataEdge(target_C, RECEIVER, target_n).build();

        List<Instance> instances = InstanceFinder.findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(pattern));
    }
}
