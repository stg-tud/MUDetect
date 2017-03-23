package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.someOverlap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static utils.SetUtils.asSet;

public class AverageWeightFunctionTest {
    @Test
    public void takesOverSingleWeight() throws Exception {
        Overlap instance = someOverlap();
        Overlaps overlaps = new Overlaps();
        Model model = () -> asSet(instance.getPattern());
        ViolationWeightFunction weightFunction = new AverageWeightFunction(w((v, os, m) -> 42f));

        float weight = weightFunction.getWeight(instance, overlaps, model);

        assertThat(weight, is(42f));
    }

    @Test
    public void addsAndNormalizesTwoWeights() throws Exception {
        Overlap instance = someOverlap();
        Overlaps overlaps = new Overlaps();
        Model model = () -> asSet(instance.getPattern());
        ViolationWeightFunction weightFunction = new AverageWeightFunction(
                w((v, os, m) -> 2f),
                w((v, os, m) -> 4f));

        float weight = weightFunction.getWeight(instance, overlaps, model);

        assertThat(weight, is(3f));
    }

    private static ViolationWeightFunction w(Function<Overlap, Overlaps, Model, Float> weight) {
        return new ViolationWeightFunction() {
            @Override
            public float getWeight(Overlap violation, Overlaps overlaps, Model model) {
                return weight.apply(violation, overlaps, model);
            }

            @Override
            public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @FunctionalInterface
    interface Function<A, B, C, R> {
        public R apply(A a, B b, C c);
    }
}
