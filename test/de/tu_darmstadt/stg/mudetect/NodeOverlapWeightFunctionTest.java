package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Before;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NodeOverlapWeightFunctionTest {

    private Instance violation;

    @Before
    public void setup() {
        TestAUGBuilder aTargetBuilder = buildAUG().withActionNode("a");
        TestAUGBuilder patternBuilder = buildAUG().withActionNodes("a", "b");
        violation = buildInstance(aTargetBuilder, patternBuilder).withNode("a", "a").build();
    }

    @Test
    public void calculatesOverlapWeight() throws Exception {
        ViolationWeightFunction weightFunction = new NodeOverlapWeightFunction();

        float weight = weightFunction.getWeight(violation, null, null);

        assertThat(weight, is(0.5f));
    }

}
