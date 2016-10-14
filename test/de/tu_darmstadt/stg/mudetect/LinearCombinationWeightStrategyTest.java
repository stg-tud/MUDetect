package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.someInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static utils.SetUtils.asSet;

public class LinearCombinationWeightStrategyTest {
    @Test
    public void combinesSingleWeight() throws Exception {
        Instance instance = someInstance();
        Overlaps overlaps = new Overlaps();
        Model model = () -> asSet(instance.getPattern());
        ViolationWeightStrategy strategy = new LinearCombinationWeightStrategy((v, os, m) -> 42f);

        float weight = strategy.getWeight(instance, overlaps, model);

        assertThat(weight, is(42f));
    }

    @Test
    public void combinesTwoWeights() throws Exception {
        Instance instance = someInstance();
        Overlaps overlaps = new Overlaps();
        Model model = () -> asSet(instance.getPattern());
        ViolationWeightStrategy strategy = new LinearCombinationWeightStrategy(
                (v, os, m) -> 42f,
                (v, os, m) -> 23f);

        float weight = strategy.getWeight(instance, overlaps, model);

        assertThat(weight, is(42f + 23f));
    }

    private class LinearCombinationWeightStrategy implements ViolationWeightStrategy {
        private final ViolationWeightStrategy[] strategies;

        public LinearCombinationWeightStrategy(ViolationWeightStrategy... strategies) {
            this.strategies = strategies;
        }

        @Override
        public float getWeight(Instance violation, Overlaps overlaps, Model model) {
            float weight = 0;
            for (ViolationWeightStrategy strategy : strategies) {
                weight += strategy.getWeight(violation, overlaps, model);
            }
            return weight;
        }
    }
}
