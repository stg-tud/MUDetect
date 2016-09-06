package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import egroum.EGroumActionNode;
import egroum.EGroumDataEdge;
import egroum.EGroumDataNode;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.eko.mudetect.model.AUGTestUtils.createAUG;
import static de.tu_darmstadt.stg.eko.mudetect.model.InstanceTestUtils.hasInstance;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FindInstancesTest {
    @Test
    public void findsSingleNodeInstance() throws Exception {
        AUG pattern = createAUG(new EGroumActionNode("C.m()"));

        AUG target = createAUG(new EGroumActionNode("C.m()"));

        List<Instance> instances = InstanceFinder.findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(pattern));
    }

    @Test
    public void findsCallReceiver() throws Exception {
        EGroumDataNode pattern_C = new EGroumDataNode("C");
        EGroumActionNode pattern_m = new EGroumActionNode("C.m()");
        AUG pattern = createAUG(pattern_C, pattern_m);
        pattern.addEdge(pattern_C, pattern_m, new EGroumDataEdge(pattern_C, pattern_m, EGroumDataEdge.Type.RECEIVER));


        EGroumDataNode target_C = new EGroumDataNode("C");
        EGroumActionNode target_m = new EGroumActionNode("C.m()");
        AUG target = createAUG(target_C, target_m);
        target.addEdge(target_C, target_m, new EGroumDataEdge(target_C, target_m, EGroumDataEdge.Type.RECEIVER));

        List<Instance> instances = InstanceFinder.findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(pattern));
    }

    @Test
    public void findsMultipleCalls() throws Exception {
        EGroumDataNode pattern_C = new EGroumDataNode("C");
        EGroumActionNode pattern_m = new EGroumActionNode("C.m()");
        EGroumActionNode pattern_n = new EGroumActionNode("C.n()");
        AUG pattern = createAUG(pattern_C, pattern_m, pattern_n);
        pattern.addEdge(pattern_C, pattern_m, new EGroumDataEdge(pattern_C, pattern_m, EGroumDataEdge.Type.RECEIVER));
        pattern.addEdge(pattern_C, pattern_n, new EGroumDataEdge(pattern_C, pattern_n, EGroumDataEdge.Type.RECEIVER));

        EGroumDataNode target_C = new EGroumDataNode("C");
        EGroumActionNode target_m = new EGroumActionNode("C.m()");
        EGroumActionNode target_n = new EGroumActionNode("C.n()");
        AUG target = createAUG(target_C, target_m, target_n);
        target.addEdge(target_C, target_m, new EGroumDataEdge(target_C, target_m, EGroumDataEdge.Type.RECEIVER));
        target.addEdge(target_C, target_n, new EGroumDataEdge(target_C, target_n, EGroumDataEdge.Type.RECEIVER));

        List<Instance> instances = InstanceFinder.findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(pattern));
    }
}
