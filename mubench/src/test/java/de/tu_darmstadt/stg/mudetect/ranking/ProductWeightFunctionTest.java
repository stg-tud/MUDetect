package de.tu_darmstadt.stg.mudetect.ranking;

import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.someOverlap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static de.tu_darmstadt.stg.mudetect.utils.SetUtils.asSet;

public class ProductWeightFunctionTest {
    @Test
    public void multipliesWeights() throws Exception {
        Overlap instance = someOverlap();
        Overlaps overlaps = new Overlaps();
        Model model = () -> asSet(instance.getPattern());

        ProductWeightFunction weightFunction = new ProductWeightFunction(w((o, os, m) -> 0.5), w((o, os, m) -> 0.5));

        assertThat(weightFunction.getWeight(instance, overlaps, model), is(0.25));
    }

    private static ViolationWeightFunction w(AverageWeightFunctionTest.Function<Overlap, Overlaps, Model, Double> weight) {
        return new ViolationWeightFunction() {
            @Override
            public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
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
        R apply(A a, B b, C c);
    }
}
