package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import egroum.EGroumActionNode;
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
        AUG pattern = createAUG(new EGroumActionNode("C.m()")).build();

        AUG target = createAUG(new EGroumActionNode("C.m()")).build();

        List<Instance> instances = InstanceFinder.findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(pattern));
    }

}
