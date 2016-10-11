package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.someInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MuDetectTest {
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Mock
    private Model model;
    @Mock
    private InstanceFinder instanceFinder;
    @Mock
    private ViolationFactory violationFactory;

    @Test
    public void findsViolations() throws Exception {
        final Pattern pattern = somePattern();
        final AUG target = someAUG();
        final Collection<AUG> targets = singletonList(target);
        final Instance instance = someInstance(pattern, target);
        final Violation violation = new Violation(instance, 1);
        final ViolationRankingStrategy rankingStrategy = new NoRankingStrategy();

        context.checking(new Expectations() {{
            oneOf(model).getPatterns(); will(returnValue(Collections.singleton(pattern)));
            oneOf(instanceFinder).findInstances(target, pattern); will(returnValue(singletonList(instance)));
            oneOf(violationFactory).isViolation(instance); will(returnValue(true));
        }});

        MuDetect muDetect = new MuDetect(model, instanceFinder, violationFactory, rankingStrategy);
        List<Violation> violations = muDetect.findViolations(targets);

        assertThat(violations, hasSize(1));
        assertThat(violations, hasItem(violation));
    }

    @Test
    public void ignoresNonViolations() throws Exception {
        final Pattern pattern = somePattern();
        final AUG target = someAUG();
        final Collection<AUG> targets = singletonList(target);
        final Instance instance = someInstance(pattern, target);
        final ViolationRankingStrategy rankingStrategy = new NoRankingStrategy();

        context.checking(new Expectations() {{
            oneOf(model).getPatterns(); will(returnValue(Collections.singleton(pattern)));
            oneOf(instanceFinder).findInstances(target, pattern); will(returnValue(singletonList(instance)));
            allowing(violationFactory).isViolation(with(any(Instance.class))); will(returnValue(false));
        }});

        MuDetect muDetect = new MuDetect(model, instanceFinder, violationFactory, rankingStrategy);
        List<Violation> violations = muDetect.findViolations(targets);

        assertThat(violations, is(empty()));
    }
}
