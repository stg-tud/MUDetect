package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.emptyOverlap;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.someOverlap;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static utils.SetUtils.asSet;

public class PatternSupportWeightFunctionTest {
    @Test
    public void computesSinglePatternWeight() throws Exception {
        Overlap overlap = someOverlap();
        Pattern pattern = overlap.getPattern();
        Model model = () -> asSet(pattern);
        ViolationWeightFunction weightFunction = new PatternSupportWeightFunction();

        float weight = weightFunction.getWeight(overlap, null, model);

        assertThat(weight, is(1f));
    }

    @Test
    public void weightsRelativeToLargestSupport() throws Exception {
        Pattern pattern = somePattern(2);
        Model model = () -> asSet(pattern, somePattern(4));
        ViolationWeightFunction weigthFunction = new PatternSupportWeightFunction();

        float weight = weigthFunction.getWeight(emptyOverlap(pattern), null, model);

        assertThat(weight, is(0.5f));
    }

}
