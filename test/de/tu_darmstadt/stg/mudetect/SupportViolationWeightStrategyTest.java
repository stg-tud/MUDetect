package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.emptyInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.someInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static utils.SetUtils.asSet;

public class SupportViolationWeightStrategyTest {
    @Test
    public void computesSinglePatternWeight() throws Exception {
        Instance instance = someInstance();
        Pattern pattern = instance.getPattern();
        Model model = () -> asSet(pattern);
        ViolationWeightStrategy weightStrategy = new SupportPatternWeightStrategy();

        float weight = weightStrategy.getWeight(instance, null, model);

        assertThat(weight, is(1f));
    }

    @Test
    public void weightsRelativeToLargestSupport() throws Exception {
        Pattern pattern = somePattern(2);
        Model model = () -> asSet(pattern, somePattern(4));
        ViolationWeightStrategy weightStrategy = new SupportPatternWeightStrategy();

        float weight = weightStrategy.getWeight(emptyInstance(pattern), null, model);

        assertThat(weight, is(0.5f));
    }

}
