package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.emptyOverlap;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.someOverlap;
import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.somePattern;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static de.tu_darmstadt.stg.mudetect.utils.SetUtils.asSet;

public class PatternSupportWeightFunctionTest {
    @Test
    public void computesSinglePatternWeight() throws Exception {
        Overlap overlap = someOverlap();
        APIUsagePattern pattern = overlap.getPattern();
        Model model = () -> asSet(pattern);
        ViolationWeightFunction weightFunction = new PatternSupportWeightFunction();

        double weight = weightFunction.getWeight(overlap, null, model);

        assertThat(weight, is(1.0));
    }

    @Test
    public void weightsRelativeToLargestSupport() throws Exception {
        APIUsagePattern pattern = somePattern(2);
        Model model = () -> asSet(pattern, somePattern(4));
        ViolationWeightFunction weigthFunction = new PatternSupportWeightFunction();

        double weight = weigthFunction.getWeight(emptyOverlap(pattern), null, model);

        assertThat(weight, is(0.5));
    }

}
