package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mudetect.ranking.NoRankingStrategy;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.someAUG;
import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.somePattern;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.someOverlap;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MuDetectTest {
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Mock
    private Model model;
    @Mock
    private OverlapsFinder overlapsFinder;
    @Mock
    private ViolationPredicate violationPredicate;

    @Test
    public void findsViolations() {
        final APIUsagePattern pattern = somePattern();
        final APIUsageExample target = someAUG();
        final Collection<APIUsageExample> targets = singletonList(target);
        final Overlap overlap = someOverlap(pattern, target);
        final Violation violation = new Violation(overlap, 1, "constant rank");
        final BiFunction<Overlaps, Model, List<Violation>> rankingStrategy = new NoRankingStrategy()::rankViolations;

        context.checking(new Expectations() {{
            oneOf(model).getPatterns(); will(returnValue(Collections.singleton(pattern)));
            oneOf(overlapsFinder).findOverlaps(target, pattern); will(returnValue(singletonList(overlap)));
            oneOf(violationPredicate).apply(overlap); will(returnValue(Optional.of(true)));
        }});

        MuDetect muDetect = new MuDetect(model, overlapsFinder, violationPredicate, rankingStrategy);
        List<Violation> violations = muDetect.findViolations(targets);

        assertThat(violations, hasSize(1));
        assertThat(violations, hasItem(violation));
    }

    @Test
    public void ignoresNonViolations() {
        final APIUsagePattern pattern = somePattern();
        final APIUsageExample target = someAUG();
        final Collection<APIUsageExample> targets = singletonList(target);
        final Overlap overlap = someOverlap(pattern, target);
        final BiFunction<Overlaps, Model, List<Violation>> rankingStrategy = new NoRankingStrategy()::rankViolations;

        context.checking(new Expectations() {{
            oneOf(model).getPatterns(); will(returnValue(Collections.singleton(pattern)));
            oneOf(overlapsFinder).findOverlaps(target, pattern); will(returnValue(singletonList(overlap)));
            allowing(violationPredicate).apply(with(any(Overlap.class))); will(returnValue(Optional.of(false)));
        }});

        MuDetect muDetect = new MuDetect(model, overlapsFinder, violationPredicate, rankingStrategy);
        List<Violation> violations = muDetect.findViolations(targets);

        assertThat(violations, is(empty()));
    }
}
