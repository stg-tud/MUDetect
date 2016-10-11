package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.someInstance;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class ConfidenceViolationRankingStrategyTest {
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Test
    public void ranksViolations() throws Exception {
        Instance violation1 = someInstance();
        Instance violation2 = someInstance();

        Overlaps overlaps = new Overlaps();
        overlaps.addViolation(violation1);
        overlaps.addViolation(violation2);

        Model model = context.mock(Model.class);

        ConfidenceCalculator confidenceCalculator = context.mock(ConfidenceCalculator.class);
        context.checking(new Expectations() {{
            allowing(confidenceCalculator).getConfidence(violation1, overlaps, model); will(returnValue(0.5f));
            allowing(confidenceCalculator).getConfidence(violation2, overlaps, model); will(returnValue(0.7f));
        }});

        ViolationRankingStrategy strategy = new ConfidenceViolationRankingStrategy(confidenceCalculator);
        final List<Violation> violations = strategy.rankViolations(overlaps, model);

        assertThat(violations, contains(new Violation(violation2, 0.7f), new Violation(violation1, 0.5f)));
    }

    @Test
    public void ranksViolationsWithSameConfidenceInAnyOrder() throws Exception {
        Instance violation1 = someInstance();
        Instance violation2 = someInstance();

        Overlaps overlaps = new Overlaps();
        overlaps.addViolation(violation1);
        overlaps.addViolation(violation2);

        Model model = context.mock(Model.class);

        ConfidenceCalculator confidenceCalculator = context.mock(ConfidenceCalculator.class);
        context.checking(new Expectations() {{
            allowing(confidenceCalculator).getConfidence(violation1, overlaps, model); will(returnValue(1f));
            allowing(confidenceCalculator).getConfidence(violation2, overlaps, model); will(returnValue(1f));
        }});

        ViolationRankingStrategy strategy = new ConfidenceViolationRankingStrategy(confidenceCalculator);
        final List<Violation> violations = strategy.rankViolations(overlaps, model);

        assertThat(violations, containsInAnyOrder(new Violation(violation2, 1f), new Violation(violation1, 1f)));
    }


}
