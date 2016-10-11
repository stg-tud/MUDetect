package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

public class CalculateViolationConfidenceTest {

    private TestAUGBuilder patternBuilder;
    private Pattern pattern;
    private Model model;
    private Instance violation;
    private Overlaps overlaps;

    @Before
    public void setup() {
        TestAUGBuilder aTargetBuilder = buildAUG().withActionNode("a");

        patternBuilder = buildAUG().withActionNodes("a", "b");
        pattern = somePattern(patternBuilder);
        model = () -> asSet(pattern);

        violation = buildInstance(aTargetBuilder, patternBuilder).withNode("a", "a").build();
        overlaps = new Overlaps();
        overlaps.addViolation(violation);
    }

    @Test
    public void calculatesPatternSupportWeight_noEquallySizedPattern() throws Exception {
        SupportConfidenceCalculator calculator = new SupportConfidenceCalculator(1, 0, 0);

        float confidence = calculator.getConfidence(violation, overlaps, model);

        assertThat(confidence, is(1f));
    }

    @Test
    public void calculatesPatternSupportWeight_equallySizedPatternWithLargerSupport() throws Exception {
        Pattern pattern2 = new Pattern(pattern.getAUG(), pattern.getSupport() * 2);
        model = () -> asSet(pattern, pattern2);
        SupportConfidenceCalculator calculator = new SupportConfidenceCalculator(1, 0, 0);

        float confidence = calculator.getConfidence(violation, overlaps, model);

        assertThat(confidence, is(0.5f));
    }

    @Test
    public void calculatesPatternSupportWeight_equallySizedPatternWithSmallerSupport() throws Exception {
        Pattern pattern2 = new Pattern(pattern.getAUG(), 1);
        model = () -> asSet(pattern, pattern2);
        SupportConfidenceCalculator calculator = new SupportConfidenceCalculator(1, 0, 0);

        float confidence = calculator.getConfidence(violation, overlaps, model);

        assertThat(confidence, is(1f));
    }

    @Test
    public void calculatesOverlapWeight() throws Exception {
        final SupportConfidenceCalculator calculator = new SupportConfidenceCalculator(0, 1, 0);

        final float confidence = calculator.getConfidence(violation, overlaps, model);

        assertThat(confidence, is(0.5f));
    }

    @Test
    public void calculatesViolationSupportWeight_noEqualViolations() throws Exception {
        final SupportConfidenceCalculator calculator = new SupportConfidenceCalculator(0, 0, 1);

        final float confidence = calculator.getConfidence(violation, overlaps, model);

        assertThat(confidence, is(1f));
    }

    @Test
    public void calculatesViolationSupportWeight_anEqualViolation() throws Exception {
        TestAUGBuilder anotherTarget = buildAUG().withActionNode("a");
        Instance anEqualViolation = buildInstance(anotherTarget, patternBuilder).withNode("a", "a").build();
        overlaps.addViolation(anEqualViolation);
        final SupportConfidenceCalculator calculator = new SupportConfidenceCalculator(0, 0, 1);

        final float confidence = calculator.getConfidence(violation, overlaps, model);

        assertThat(confidence, is(0.5f));
    }

    @Test
    public void normalizesConfidence() throws Exception {
        final SupportConfidenceCalculator calculator = new SupportConfidenceCalculator(1, 1, 1);

        float confidence = calculator.getConfidence(violation, overlaps, model);

        assertThat(confidence, is(lessThanOrEqualTo(1f)));
    }

    @SafeVarargs
    private static <T> Set<T> asSet(T... elements) {
        return new HashSet<T>(Arrays.asList(elements));
    }
}
