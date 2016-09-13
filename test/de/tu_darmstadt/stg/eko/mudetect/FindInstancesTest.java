package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import de.tu_darmstadt.stg.eko.mudetect.model.AUGBuilder;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.eko.mudetect.model.AUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.eko.mudetect.model.InstanceTestUtils.hasInstance;
import static egroum.EGroumDataEdge.Type.CONDITION;
import static egroum.EGroumDataEdge.Type.RECEIVER;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FindInstancesTest {
    @Test
    public void findsSingleNodeInstance() throws Exception {
        assertFindsInstance(buildAUG().withActionNode("C.m()"));
    }

    @Test
    public void findsCallReceiver() throws Exception {
        assertFindsInstance(buildAUG().withDataNode("C").withActionNode("C.m()").withDataEdge("C", RECEIVER, "C.m()"));
    }

    @Test
    public void findsMultipleCalls() throws Exception {
        assertFindsInstance(buildAUG().withDataNode("C").withActionNodes("C.m()", "C.n()")
                .withDataEdge("C", RECEIVER, "C.m()")
                .withDataEdge("C", RECEIVER, "C.n()"));
    }

    @Test
    public void findCallArguments() throws Exception {
        assertFindsInstance(buildAUG().withDataNode("Object").withActionNode("Object.equals()")
                .withDataEdge("Object", PARAMETER, "Object.equals()"));
    }

    @Test
    public void findsConditionEdge() throws Exception {
        assertFindsInstance(buildAUG().withDataNode("Iterator").withActionNodes("Iterator.hasNext()", "Iterator.next()")
                .withDataEdge("Iterator", RECEIVER, "Iterator.hasNext()")
                .withDataEdge("Iterator", RECEIVER, "Iterator.next()")
                .withDataEdge("Iterator.hasNext()", CONDITION, "Iterator.next()"));
    }

    private void assertFindsInstance(AUGBuilder builder) {
        AUG pattern = builder.build();
        AUG target = builder.build();

        List<Instance> instances = Instance.findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasInstance(pattern));
    }
}
