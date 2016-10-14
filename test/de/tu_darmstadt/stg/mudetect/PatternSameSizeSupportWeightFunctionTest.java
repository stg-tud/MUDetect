package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Before;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static utils.SetUtils.asSet;

public class PatternSameSizeSupportWeightFunctionTest {
    private Pattern pattern;
    private Model model;
    private Instance violation;

    @Before
    public void setup() {
        TestAUGBuilder aTargetBuilder = buildAUG().withActionNode("a");

        TestAUGBuilder patternBuilder = buildAUG().withActionNodes("a", "b");
        pattern = somePattern(patternBuilder);
        model = () -> asSet(pattern);

        violation = buildInstance(aTargetBuilder, patternBuilder).withNode("a", "a").build();
    }

    @Test
    public void calculatesPatternSupportWeight_noEquallySizedPattern() throws Exception {
        ViolationWeightFunction weightFunction = new PatternSameSizeSupportWeightFunction();

        float weight = weightFunction.getWeight(violation, null, model);

        assertThat(weight, is(1f));
    }

    @Test
    public void calculatesPatternSupportWeight_equallySizedPatternWithLargerSupport() throws Exception {
        Pattern pattern2 = somePattern(pattern, pattern.getSupport() * 2);
        model = () -> asSet(pattern, pattern2);
        ViolationWeightFunction weightFunction = new PatternSameSizeSupportWeightFunction();

        float weight = weightFunction.getWeight(violation, null, model);

        assertThat(weight, is(0.5f));
    }

    @Test
    public void calculatesPatternSupportWeight_equallySizedPatternWithSmallerSupport() throws Exception {
        Pattern pattern2 = somePattern(pattern, 1);
        model = () -> asSet(pattern, pattern2);
        ViolationWeightFunction weightFunction = new PatternSameSizeSupportWeightFunction();

        float weight = weightFunction.getWeight(violation, null, model);

        assertThat(weight, is(1f));
    }

}
