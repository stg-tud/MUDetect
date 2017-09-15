package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.mining.Pattern;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.mining.TestPatternBuilder.somePattern;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.instance;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static utils.SetUtils.asSet;

public class PatternSizeWeightFunctionTest {

    @Test
    public void weightsLargerPatternMore() throws Exception {
        Pattern smallerPattern = somePattern(buildAUG().withActionNode("N").build());
        Pattern largerPattern = somePattern(buildAUG().withActionNodes("N", "M").build());
        Model model = () -> asSet(smallerPattern, largerPattern);

        double smallerWeight = new PatternSizeWeightFunction().getWeight(instance(smallerPattern), null, model);
        double largerWeight = new PatternSizeWeightFunction().getWeight(instance(largerPattern), null, model);

        assertThat(smallerWeight, is(lessThan(largerWeight)));
    }

    @Test
    public void normalizesWeight() throws Exception {
        Pattern smallerPattern = somePattern(buildAUG().withActionNodes("N", "M").build());
        Pattern largerPattern = somePattern(buildAUG().withActionNodes("N", "M", "O").build());
        Model model = () -> asSet(smallerPattern, largerPattern);

        double smallerWeight = new PatternSizeWeightFunction().getWeight(instance(smallerPattern), null, model);

        assertThat(smallerWeight, is(lessThan(1.0)));
        assertThat(smallerWeight, is(greaterThan(0.0)));
    }

    @Test
    public void smoothesWeight() throws Exception {
        Pattern smallerPattern = somePattern(buildAUG().withActionNodes("N").build());
        Pattern largerPattern = somePattern(buildAUG().withActionNodes("N", "M").build());
        Pattern largestPattern = somePattern(buildAUG().withActionNodes("N", "M", "O").build());
        Model model = () -> asSet(smallerPattern, largerPattern, largestPattern);

        double smallerWeight = new PatternSizeWeightFunction().getWeight(instance(smallerPattern), null, model);
        double largerWeight = new PatternSizeWeightFunction().getWeight(instance(largerPattern), null, model);
        double largestWeight = new PatternSizeWeightFunction().getWeight(instance(largestPattern), null, model);

        assertThat(largerWeight - smallerWeight, is(greaterThan(largestWeight - largerWeight)));
    }

}
