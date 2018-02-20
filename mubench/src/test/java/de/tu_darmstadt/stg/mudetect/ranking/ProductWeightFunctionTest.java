package de.tu_darmstadt.stg.mudetect.ranking;

import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.someOverlap;
import static de.tu_darmstadt.stg.mudetect.ranking.RankingTestUtils.w;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static de.tu_darmstadt.stg.mudetect.utils.SetUtils.asSet;

public class ProductWeightFunctionTest {
    @Test
    public void multipliesWeights() {
        Overlap instance = someOverlap();
        Overlaps overlaps = new Overlaps();
        Model model = () -> asSet(instance.getPattern());

        ProductWeightFunction weightFunction = new ProductWeightFunction(w((o, os, m) -> 0.5), w((o, os, m) -> 0.5));

        assertThat(weightFunction.getWeight(instance, overlaps, model), is(0.25));
    }
}
