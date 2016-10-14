package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Before;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ViolationSupportWeightFunctionTest {

    private TestAUGBuilder patternBuilder;
    private Instance violation;
    private Overlaps overlaps;

    @Before
    public void setup() {
        TestAUGBuilder aTargetBuilder = buildAUG().withActionNode("a");

        patternBuilder = buildAUG().withActionNodes("a", "b");

        violation = buildInstance(aTargetBuilder, patternBuilder).withNode("a", "a").build();
        overlaps = new Overlaps();
        overlaps.addViolation(violation);
    }

    @Test
    public void withoutEqualViolations() throws Exception {
        ViolationWeightFunction weightFunction = new ViolationSupportWeightFunction();

        float weight = weightFunction.getWeight(violation, overlaps, null);

        assertThat(weight, is(1f));
    }

    @Test
    public void calculatesViolationSupportWeight_anEqualViolation() throws Exception {
        TestAUGBuilder anotherTarget = buildAUG().withActionNode("a");
        Instance anEqualViolation = buildInstance(anotherTarget, patternBuilder).withNode("a", "a").build();
        overlaps.addViolation(anEqualViolation);
        ViolationWeightFunction weightFunction = new ViolationSupportWeightFunction();

        float weight = weightFunction.getWeight(violation, overlaps, null);

        assertThat(weight, is(0.5f));
    }

}
