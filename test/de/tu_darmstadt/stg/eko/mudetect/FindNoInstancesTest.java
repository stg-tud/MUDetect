package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import egroum.EGroumActionNode;
import egroum.EGroumDataNode;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.eko.mudetect.model.AUGBuilder.newAUG;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

public class FindNoInstancesTest {
    @Test
    public void ignoresNonOverlappingTarget() throws Exception {
        AUG pattern = newAUG().withActionNode("F.foo()").build();
        AUG target = newAUG().withActionNode("B.bar()").build();

        List<Instance> instances = InstanceFinder.findInstances(target, pattern);

        assertThat(instances, is(empty()));
    }

    @Test
    public void ignoresTrivialOverlap() throws Exception {
        AUG pattern = newAUG().withDataNode("Object").build();
        AUG target = newAUG().withDataNode("Object").build();

        List<Instance> instances = InstanceFinder.findInstances(target, pattern);

        assertThat(instances, is(empty()));
    }
}
