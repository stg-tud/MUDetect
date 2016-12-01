package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.extend;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static egroum.EGroumDataEdge.Type.ORDER;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OverlapWeightFunctionTest {

    @Test
    public void considersMissingNodes() throws Exception {
        TestAUGBuilder targetBuilder = buildAUG().withActionNode("a");
        TestAUGBuilder patternBuilder = extend(targetBuilder).withActionNode("b");
        Overlap violation = buildOverlap(targetBuilder, patternBuilder).withNode("a", "a").build();
        ViolationWeightFunction weightFunction = new OverlapWeightFunction();

        float weight = weightFunction.getWeight(violation, null, null);

        assertThat(weight, is(0.5f));
    }

    @Test
    public void considersMissingEdges() throws Exception {
        TestAUGBuilder targetBuilder = buildAUG().withActionNodes("a", "b");
        TestAUGBuilder patternBuilder = extend(targetBuilder).withDataEdge("a", ORDER, "b");
        Overlap violation = buildOverlap(targetBuilder, patternBuilder).withNode("a", "a").build();
        ViolationWeightFunction weightFunction = new OverlapWeightFunction();

        float weight = weightFunction.getWeight(violation, null, null);

        assertThat(weight, is(1/3f));
    }
}
