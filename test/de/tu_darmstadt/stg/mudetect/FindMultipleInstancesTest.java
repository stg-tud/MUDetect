package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.AUGBuilder;
import org.junit.Test;

import java.util.*;

import static de.tu_darmstadt.stg.mudetect.InstanceBuilder.createInstance;
import static de.tu_darmstadt.stg.mudetect.model.AUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.AUGBuilder.extend;
import static egroum.EGroumDataEdge.Type.ORDER;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FindMultipleInstancesTest {
    @Test
    public void mapsAnEdgeOnlyOnce() throws Exception {
        AUGBuilder builder = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        AUGBuilder targetBuilder = extend(builder).withActionNode("B2", "B").withDataEdge("A", ORDER, "B2");

        Instance instance1 = createInstance(targetBuilder, builder)
                .withNode("A", "A")
                .withNode("B", "B")
                .withEdge("A", "A", ORDER, "B", "B").build();
        Instance instance2 = createInstance(targetBuilder, builder)
                .withNode("A", "A")
                .withNode("B2", "B")
                .withEdge("A", "A", ORDER, "B2", "B").build();

        AUG pattern = builder.build();
        AUG target = targetBuilder.build();

        List<Instance> instances = new GreedyInstanceFinder().findInstances(target, pattern);

        assertThat(instances, hasSize(2));
        assertThat(instances, hasItems(instance1, instance2));
    }

}
