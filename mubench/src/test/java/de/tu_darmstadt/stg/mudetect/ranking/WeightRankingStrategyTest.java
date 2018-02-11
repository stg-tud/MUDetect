package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.ViolationRankingStrategy;
import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.someAUG;
import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.somePattern;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.someOverlap;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class WeightRankingStrategyTest {
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Test
    public void ranksViolations() {
        Overlap violation1 = someOverlap();
        Overlap violation2 = someOverlap();

        Overlaps overlaps = new Overlaps();
        overlaps.addViolation(violation1);
        overlaps.addViolation(violation2);

        Model model = context.mock(Model.class);

        ViolationWeightFunction weightFunction = context.mock(ViolationWeightFunction.class);
        context.checking(new Expectations() {{
            allowing(weightFunction).getWeight(violation1, overlaps, model); will(returnValue(0.5));
            allowing(weightFunction).getFormula(violation1, overlaps, model); will(returnValue("0.5"));
            allowing(weightFunction).getWeight(violation2, overlaps, model); will(returnValue(0.7));
            allowing(weightFunction).getFormula(violation2, overlaps, model); will(returnValue("0.7"));
        }});

        ViolationRankingStrategy strategy = new WeightRankingStrategy(weightFunction);
        final List<Violation> violations = strategy.rankViolations(overlaps, model);

        assertThat(violations, contains(
                new Violation(violation2, 0.7, "0.7"),
                new Violation(violation1, 0.5, "0.5")));
    }

    @Test
    public void ranksViolationsWithSameConfidenceByTargetName() {
        Overlap violation1 = someOverlap(somePattern(), someAUG("target-b"));
        Overlap violation2 = someOverlap(somePattern(), someAUG("target-a"));
        Overlap violation3 = someOverlap(somePattern(), someAUG("target-c"));

        Overlaps overlaps = new Overlaps();
        overlaps.addViolation(violation1);
        overlaps.addViolation(violation2);
        overlaps.addViolation(violation3);

        Model model = context.mock(Model.class);

        ViolationWeightFunction weightFunction = context.mock(ViolationWeightFunction.class);
        context.checking(new Expectations() {{
            allowing(weightFunction).getWeight(with(any(Overlap.class)), with(same(overlaps)), with(same(model))); will(returnValue(1.0));
            allowing(weightFunction).getFormula(with(any(Overlap.class)), with(same(overlaps)), with(same(model))); will(returnValue("1"));
        }});

        ViolationRankingStrategy strategy = new WeightRankingStrategy(weightFunction);
        final List<Violation> violations = strategy.rankViolations(overlaps, model);

        assertThat(violations, contains(
                new Violation(violation2, 1.0, "1"),
                new Violation(violation1, 1.0, "1"),
                new Violation(violation3, 1.0, "1")));
    }
}
