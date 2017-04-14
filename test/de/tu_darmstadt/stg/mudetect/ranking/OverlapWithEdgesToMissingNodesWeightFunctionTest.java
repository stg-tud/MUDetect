package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.instance;
import static egroum.EGroumDataEdge.Type.ORDER;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class OverlapWithEdgesToMissingNodesWeightFunctionTest {
    @Test
    public void noMissingElements() throws Exception {
        Overlap instance = instance(buildAUG().withActionNode("A"));

        double weight = new OverlapWithEdgesToMissingNodesWeightFunction().getWeight(instance, null, null);

        assertThat(weight, is(1.0));
    }

    @Test
    public void noMissingElements_withEdge() throws Exception {
        Overlap instance = instance(buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B"));

        double weight = new OverlapWithEdgesToMissingNodesWeightFunction().getWeight(instance, null, null);

        assertThat(weight, is(1.0));
    }

    @Test
    public void includesEdgeToMissingNode() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNode("A");
        Overlap violation = buildOverlap(target, pattern).withNode("A").build();

        double weight = new OverlapWithEdgesToMissingNodesWeightFunction().getWeight(violation, null, null);

        assertThat(weight, is(closeTo(0.666, 0.001)));
    }

}
