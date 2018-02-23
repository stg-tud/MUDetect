package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.extend;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

public class OverlapWeightFunctionTest {

    @Test
    public void considersMissingNodes() {
        TestAUGBuilder targetBuilder = buildAUG().withActionNode("a");
        TestAUGBuilder patternBuilder = extend(targetBuilder).withActionNode("b");
        Overlap violation = buildOverlap(patternBuilder, targetBuilder).withNode("a", "a").build();
        ViolationWeightFunction weightFunction = new OverlapWeightFunction();

        double weight = weightFunction.getWeight(violation, null, null);

        assertThat(weight, is(0.5));
    }

    @Test
    public void considersMissingEdges() {
        TestAUGBuilder targetBuilder = buildAUG().withActionNodes("a", "b");
        TestAUGBuilder patternBuilder = extend(targetBuilder).withEdge("a", ORDER, "b");
        Overlap violation = buildOverlap(patternBuilder, targetBuilder).withNode("a", "a").build();
        ViolationWeightFunction weightFunction = new OverlapWeightFunction();

        double weight = weightFunction.getWeight(violation, null, null);

        assertThat(weight, is(closeTo(1/3.0, 0.00001)));
    }
}
