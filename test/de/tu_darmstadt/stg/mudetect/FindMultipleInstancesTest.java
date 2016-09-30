package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.extend;
import static egroum.EGroumDataEdge.Type.ORDER;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FindMultipleInstancesTest {
    @Test
    public void mapsAnEdgeOnlyOnce() throws Exception {
        TestAUGBuilder builder = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder targetBuilder = extend(builder).withActionNode("B2", "B").withDataEdge("A", ORDER, "B2");

        Instance instance1 = buildInstance(targetBuilder, builder)
                .withNode("A", "A")
                .withNode("B", "B")
                .withEdge("A", "A", ORDER, "B", "B").build();
        Instance instance2 = buildInstance(targetBuilder, builder)
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
