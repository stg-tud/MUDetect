package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import org.junit.Before;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.someAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.someOverlap;
import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.somePattern;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PatternViolationsWeightFunctionTest {

    private APIUsagePattern aPattern;
    private Overlap violation;
    private Overlaps overlaps;

    @Before
    public void setup() {
        APIUsageExample aTarget = someAUG();

        aPattern = somePattern();

        violation = someOverlap(aPattern, aTarget);
        overlaps = new Overlaps();
        overlaps.addViolation(violation);
    }

    @Test
    public void withoutOtherViolationOfPattern() {
        ViolationWeightFunction weightFunction = new PatternViolationsWeightFunction();

        double weight = weightFunction.getWeight(violation, overlaps, null);

        assertThat(weight, is(1.0));
    }

    @Test
    public void withOtherViolationOfSamePattern() {
        overlaps.addViolation(someOverlap(aPattern, someAUG()));
        ViolationWeightFunction weightFunction = new PatternViolationsWeightFunction();

        double weight = weightFunction.getWeight(violation, overlaps, null);

        assertThat(weight, is(0.5));
    }

    @Test
    public void withViolationOfOtherPattern() {
        overlaps.addViolation(someOverlap(somePattern(), someAUG()));
        ViolationWeightFunction weightFunction = new PatternViolationsWeightFunction();

        double weight = weightFunction.getWeight(violation, overlaps, null);

        assertThat(weight, is(1.0));
    }
}
