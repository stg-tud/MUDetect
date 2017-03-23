package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.mining.Pattern;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mudetect.ranking.PatternSupportWeightFunction;
import de.tu_darmstadt.stg.mudetect.ranking.WeightRankingStrategy;
import egroum.EGroumDataEdge;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.mining.TestPatternBuilder.somePattern;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static utils.SetUtils.asSet;

public class AlternativePatternsIntegrationTest {

    @Test
    public void reportsOnlyBestRankedViolationForAUsage() throws Exception {
        Pattern patternA = buildPattern("void a(Iterator i) { while (i.hasNext()) { i.next(); } }", 3);
        Pattern patternB = buildPattern("void b(Iterator i) { while (i.hasNext()) { i.remove(); } }", 2);
        AUG target = buildAUG("void v(Iterator i) { i.hasNext(); }");
        MuDetect detector = new MuDetect(() -> asSet(patternA, patternB),
                new AlternativeMappingsOverlapsFinder(),
                new MissingElementViolationFactory(),
                new WeightRankingStrategy(new PatternSupportWeightFunction()));

        List<Violation> violations = detector.findViolations(asSet(target));

        Overlap overlap = buildOverlap(patternA, target)
                .withNodes("Iterator", "Iterator.hasNext()")
                .withEdge("Iterator", EGroumDataEdge.Type.RECEIVER, "Iterator.hasNext()").build();
        Violation violation = new Violation(overlap, 1.0f, "(pattern support = 3) / (max pattern support = 3)");
        assertThat(violations, contains(violation));
    }

    private Pattern buildPattern(String method, int support) {
        return somePattern(buildAUG(method), support);
    }
}
