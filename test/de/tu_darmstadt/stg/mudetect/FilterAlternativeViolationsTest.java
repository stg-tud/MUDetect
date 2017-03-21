package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mudetect.ranking.AverageWeightFunction;
import de.tu_darmstadt.stg.mudetect.ranking.PatternSupportWeightFunction;
import de.tu_darmstadt.stg.mudetect.ranking.WeightRankingStrategy;
import mining.Configuration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumTestUtils.buildGroumForMethod;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FilterAlternativeViolationsTest {

    @Test
    public void reportsOnlyTopMostViolation() throws Exception {
        String patternACode = "void a(Iterator i) { while (i.hasNext()) { i.next(); } }";
        String patternBCode = "void b(Iterator i) { while (i.hasNext()) { i.remove(); } }";
        String violationCode = "void v(Iterator i) { i.hasNext(); }";

        MuDetect detector = new MuDetect(new MinedPatternsModel(
                new Configuration() {{
                    minPatternSupport = 2;
                }}, Arrays.asList(
                        buildGroumForMethod(patternACode),
                        buildGroumForMethod(patternACode),
                        buildGroumForMethod(patternACode),
                        buildGroumForMethod(patternBCode),
                        buildGroumForMethod(patternBCode))),
                new AlternativeMappingsOverlapsFinder(),
                new MissingElementViolationFactory(),
                new WeightRankingStrategy(
                        new AverageWeightFunction(
                                new PatternSupportWeightFunction())));

        List<Violation> violations = detector.findViolations(Collections.singletonList(buildAUG(violationCode)));

        assertThat(violations, hasSize(1));
        assertThat(violations.get(0).getOverlap().getMissingNodes().iterator().next().getLabel(), is("Iterator.next()"));
    }
}
