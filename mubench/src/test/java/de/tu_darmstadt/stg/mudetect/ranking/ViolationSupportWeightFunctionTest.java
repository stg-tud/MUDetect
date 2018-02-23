package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import org.junit.Before;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ViolationSupportWeightFunctionTest {

    private TestAUGBuilder patternBuilder;
    private Overlap violation;
    private Overlaps overlaps;

    @Before
    public void setup() {
        TestAUGBuilder aTargetBuilder = buildAUG().withActionNode("a");

        patternBuilder = buildAUG().withActionNodes("a", "b");

        violation = buildOverlap(patternBuilder, aTargetBuilder).withNode("a", "a").build();
        overlaps = new Overlaps();
        overlaps.addViolation(violation);
    }

    @Test
    public void withoutEqualViolations() {
        ViolationWeightFunction weightFunction = new ViolationSupportWeightFunction();

        double weight = weightFunction.getWeight(violation, overlaps, null);

        assertThat(weight, is(1.0));
    }

    @Test
    public void calculatesViolationSupportWeight_anEqualViolation() {
        TestAUGBuilder anotherTarget = buildAUG().withActionNode("a");
        Overlap anEqualViolation = buildOverlap(patternBuilder, anotherTarget).withNode("a", "a").build();
        overlaps.addViolation(anEqualViolation);
        ViolationWeightFunction weightFunction = new ViolationSupportWeightFunction();

        double weight = weightFunction.getWeight(violation, overlaps, null);

        assertThat(weight, is(0.5));
    }

}
