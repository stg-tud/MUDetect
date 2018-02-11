package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import edu.iastate.cs.mudetect.mining.Model;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.somePattern;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.instance;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static de.tu_darmstadt.stg.mudetect.utils.SetUtils.asSet;

public class PatternSizeWeightFunctionTest {

    @Test
    public void weightsLargerPatternMore() throws Exception {
        APIUsagePattern smallerPattern = somePattern(buildAUG().withActionNode("N").build());
        APIUsagePattern largerPattern = somePattern(buildAUG().withActionNodes("N", "M").build());
        Model model = () -> asSet(smallerPattern, largerPattern);

        double smallerWeight = new PatternSizeWeightFunction().getWeight(instance(smallerPattern), null, model);
        double largerWeight = new PatternSizeWeightFunction().getWeight(instance(largerPattern), null, model);

        assertThat(smallerWeight, is(lessThan(largerWeight)));
    }

    @Test
    public void normalizesWeight() throws Exception {
        APIUsagePattern smallerPattern = somePattern(buildAUG().withActionNodes("N", "M").build());
        APIUsagePattern largerPattern = somePattern(buildAUG().withActionNodes("N", "M", "O").build());
        Model model = () -> asSet(smallerPattern, largerPattern);

        double smallerWeight = new PatternSizeWeightFunction().getWeight(instance(smallerPattern), null, model);

        assertThat(smallerWeight, is(lessThan(1.0)));
        assertThat(smallerWeight, is(greaterThan(0.0)));
    }

    @Test
    public void smoothesWeight() throws Exception {
        APIUsagePattern smallerPattern = somePattern(buildAUG().withActionNodes("N").build());
        APIUsagePattern largerPattern = somePattern(buildAUG().withActionNodes("N", "M").build());
        APIUsagePattern largestPattern = somePattern(buildAUG().withActionNodes("N", "M", "O").build());
        Model model = () -> asSet(smallerPattern, largerPattern, largestPattern);

        double smallerWeight = new PatternSizeWeightFunction().getWeight(instance(smallerPattern), null, model);
        double largerWeight = new PatternSizeWeightFunction().getWeight(instance(largerPattern), null, model);
        double largestWeight = new PatternSizeWeightFunction().getWeight(instance(largestPattern), null, model);

        assertThat(largerWeight - smallerWeight, is(greaterThan(largestWeight - largerWeight)));
    }

}
