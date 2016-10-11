package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Instances;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
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
    private Instances violations;

    @Before
    public void setup() {
        TestAUGBuilder aTargetBuilder = buildAUG().withActionNode("a");

        patternBuilder = buildAUG().withActionNodes("a", "b");
        pattern = somePattern(patternBuilder);
        model = () -> asSet(pattern);

        violation = buildInstance(aTargetBuilder, patternBuilder).withNode("a", "a").build();
        violations = new Instances(violation);
    }

    @Test
    public void calculatesPatternSupportWeight_noEquallySizedPattern() throws Exception {
        ConfidenceCalculator calculator = new ConfidenceCalculator(1, 0, 0);

        float confidence = calculator.getViolationConfidence(violation, violations, model);

        assertThat(confidence, is(1f));
    }

    @Test
    public void calculatesPatternSupportWeight_equallySizedPatternWithLargerSupport() throws Exception {
        Pattern pattern2 = new Pattern(pattern.getAUG(), pattern.getSupport() * 2);
        model = () -> asSet(pattern, pattern2);
        ConfidenceCalculator calculator = new ConfidenceCalculator(1, 0, 0);

        float confidence = calculator.getViolationConfidence(violation, violations, model);

        assertThat(confidence, is(0.5f));
    }

    @Test
    public void calculatesPatternSupportWeight_equallySizedPatternWithSmallerSupport() throws Exception {
        Pattern pattern2 = new Pattern(pattern.getAUG(), 1);
        model = () -> asSet(pattern, pattern2);
        ConfidenceCalculator calculator = new ConfidenceCalculator(1, 0, 0);

        float confidence = calculator.getViolationConfidence(violation, violations, model);

        assertThat(confidence, is(1f));
    }

    @Test
    public void calculatesOverlapWeight() throws Exception {
        final ConfidenceCalculator calculator = new ConfidenceCalculator(0, 1, 0);

        final float confidence = calculator.getViolationConfidence(violation, violations, model);

        assertThat(confidence, is(0.5f));
    }

    @Test
    public void calculatesViolationSupportWeight_noEqualViolations() throws Exception {
        final ConfidenceCalculator calculator = new ConfidenceCalculator(0, 0, 1);

        final float confidence = calculator.getViolationConfidence(violation, violations, model);

        assertThat(confidence, is(1f));
    }

    @Test
    public void calculatesViolationSupportWeight_anEqualViolation() throws Exception {
        TestAUGBuilder anotherTarget = buildAUG().withActionNode("a");
        Instance anEqualViolation = buildInstance(anotherTarget, patternBuilder).withNode("a", "a").build();
        violations = new Instances(violation, anEqualViolation);
        final ConfidenceCalculator calculator = new ConfidenceCalculator(0, 0, 1);

        final float confidence = calculator.getViolationConfidence(violation, violations, model);

        assertThat(confidence, is(0.5f));
    }

    @Test
    public void normalizesConfidence() throws Exception {
        final ConfidenceCalculator calculator = new ConfidenceCalculator(1, 1, 1);

        float confidence = calculator.getViolationConfidence(violation, violations, model);

        assertThat(confidence, is(lessThanOrEqualTo(1f)));
    }

    private static class ConfidenceCalculator {
        private final float patternSupportWeightFactor;
        private final float overlapSizeWeightFactor;
        private final float violationSupportWeightFactor;

        public ConfidenceCalculator(int patternSupportWeightFactor,
                                    int overlapSizeWeightFactor,
                                    int violationSupportWeightFactor) {
            float factorSum = patternSupportWeightFactor + overlapSizeWeightFactor + violationSupportWeightFactor;
            this.patternSupportWeightFactor = patternSupportWeightFactor / factorSum;
            this.overlapSizeWeightFactor = overlapSizeWeightFactor / factorSum;
            this.violationSupportWeightFactor = violationSupportWeightFactor / factorSum;
        }

        public float getViolationConfidence(Instance violation, Instances violations, Model model) {
            return patternSupportWeightFactor * getPatternSupportWeight(violation.getPattern(), model) +
                    overlapSizeWeightFactor * getOverlapWeight(violation) +
                    violationSupportWeightFactor * getViolationSupportWeight(violation, violations);
        }

        private float getPatternSupportWeight(Pattern pattern, Model model) {
            return pattern.getSupport() / (float) model.getMaxPatternSupport(pattern.getNodeSize());
        }

        private float getOverlapWeight(Instance violation) {
            return violation.getNodeSize() / (float) violation.getPattern().getNodeSize();
        }

        private float getViolationSupportWeight(Instance violation, Instances violations) {
            float numberOfEqualViolations = 0;
            for (Instance otherViolation : violations) {
                // two violations are equal, if they violate the same aPatternBuilder in the same way,
                // i.e., if the aPatternBuilder overlap is the same.
                if (violation.isSamePatternOverlap(otherViolation)) {
                    numberOfEqualViolations++;
                }
            }
            return 1 / numberOfEqualViolations;
        }
    }

    @SafeVarargs
    private static <T> Set<T> asSet(T... elements) {
        return new HashSet<T>(Arrays.asList(elements));
    }
}
