package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.someInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static utils.SetUtils.asSet;

public class AverageWeightFunctionTest {
    @Test
    public void takesOverSingleWeight() throws Exception {
        Instance instance = someInstance();
        Overlaps overlaps = new Overlaps();
        Model model = () -> asSet(instance.getPattern());
        ViolationWeightFunction weightFunction = new AverageWeightFunction((v, os, m) -> 42f);

        float weight = weightFunction.getWeight(instance, overlaps, model);

        assertThat(weight, is(42f));
    }

    @Test
    public void addsAndNormalizesTwoWeights() throws Exception {
        Instance instance = someInstance();
        Overlaps overlaps = new Overlaps();
        Model model = () -> asSet(instance.getPattern());
        ViolationWeightFunction weightFunction = new AverageWeightFunction(
                (v, os, m) -> 2f,
                (v, os, m) -> 4f);

        float weight = weightFunction.getWeight(instance, overlaps, model);

        assertThat(weight, is(3f));
    }
}
