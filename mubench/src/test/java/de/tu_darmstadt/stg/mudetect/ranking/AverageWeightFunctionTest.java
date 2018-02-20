package de.tu_darmstadt.stg.mudetect.ranking;

import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.someOverlap;
import static de.tu_darmstadt.stg.mudetect.ranking.RankingTestUtils.w;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static de.tu_darmstadt.stg.mudetect.utils.SetUtils.asSet;

public class AverageWeightFunctionTest {
    @Test
    public void takesOverSingleWeight() {
        Overlap instance = someOverlap();
        Overlaps overlaps = new Overlaps();
        Model model = () -> asSet(instance.getPattern());
        ViolationWeightFunction weightFunction = new AverageWeightFunction(w((v, os, m) -> 42.0));

        double weight = weightFunction.getWeight(instance, overlaps, model);

        assertThat(weight, is(42.0));
    }

    @Test
    public void addsAndNormalizesTwoWeights() {
        Overlap instance = someOverlap();
        Overlaps overlaps = new Overlaps();
        Model model = () -> asSet(instance.getPattern());
        ViolationWeightFunction weightFunction = new AverageWeightFunction(
                w((v, os, m) -> 2.0),
                w((v, os, m) -> 4.0));

        double weight = weightFunction.getWeight(instance, overlaps, model);

        assertThat(weight, is(3.0));
    }

}
