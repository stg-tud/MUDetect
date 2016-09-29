package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
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
        final Collection<AUG> targets = Collections.singletonList(target);
        final Instance instance = new Instance(pattern.getAUG(), target);
        final Violation violation = new Violation(instance, 1);

        context.checking(new Expectations() {{
            allowing(model).getPatterns();
            will(returnValue(Collections.singleton(pattern)));

            allowing(instanceFinder).findInstances(target, pattern.getAUG());
            will(returnValue(Collections.singletonList(instance)));

            allowing(violationFactory).isViolation(instance);
            will(returnValue(true));
            allowing(violationFactory).createViolation(instance);
            will(returnValue(violation));
        }});

        MuDetect muDetect = new MuDetect(model, instanceFinder, violationFactory);
        List<Violation> violations = muDetect.findViolations(targets);

        assertThat(violations, hasSize(1));
        assertThat(violations, hasItem(violation));
    }

    @Test
    public void ignoresNonViolations() throws Exception {
        final Pattern pattern = somePattern();
        final AUG target = someAUG();
        final Collection<AUG> targets = Collections.singletonList(target);
        final Instance instance = new Instance(pattern.getAUG(), target);

        context.checking(new Expectations() {{
            allowing(model).getPatterns();
            will(returnValue(Collections.singleton(pattern)));

            allowing(instanceFinder).findInstances(target, pattern.getAUG());
            will(returnValue(Collections.singletonList(instance)));

            allowing(violationFactory).isViolation(instance);
            will(returnValue(false));
        }});

        MuDetect muDetect = new MuDetect(model, instanceFinder, violationFactory);
        List<Violation> violations = muDetect.findViolations(targets);

        assertThat(violations, is(empty()));
    }

    @Test
    public void ranksViolations() throws Exception {
        final Pattern pattern = somePattern();
        final AUG target = someAUG();
        final Collection<AUG> targets = Collections.singletonList(target);
        final Instance instance1 = new Instance(pattern.getAUG(), target);
        final Violation violation1 = new Violation(instance1, 0.9f);
        final Instance instance2 = new Instance(pattern.getAUG(), target);
        final Violation violation2 = new Violation(instance2, 0.7f);

        context.checking(new Expectations() {{
            oneOf(model).getPatterns();
            will(returnValue(Collections.singleton(pattern)));

            oneOf(instanceFinder).findInstances(target, pattern.getAUG());
            will(returnValue(Arrays.asList(instance2, instance1)));

            allowing(violationFactory).isViolation(with(any(Instance.class)));
            will(returnValue(true));
            oneOf(violationFactory).createViolation(with(instance1));
            will(returnValue(violation1));
            oneOf(violationFactory).createViolation(with(instance2));
            will(returnValue(violation2));
        }});

        MuDetect muDetect = new MuDetect(model, instanceFinder, violationFactory);
        List<Violation> violations = muDetect.findViolations(targets);

        assertThat(violations, contains(violation1, violation2));
    }

    @Test
    public void ranksViolationsWithSameConfidenceInAnyOrder() throws Exception {
        final Pattern pattern = somePattern();
        final AUG target = someAUG();
        final Collection<AUG> targets = Collections.singletonList(target);
        final Instance instance1 = new Instance(pattern.getAUG(), target);
        final Violation violation1 = new Violation(instance1, 1);
        final Instance instance2 = new Instance(pattern.getAUG(), target);
        final Violation violation2 = new Violation(instance2, 1);

        context.checking(new Expectations() {{
            oneOf(model).getPatterns(); will(returnValue(Collections.singleton(pattern)));
            oneOf(instanceFinder).findInstances(target, pattern.getAUG()); will(returnValue(Arrays.asList(instance2, instance1)));
            allowing(violationFactory).isViolation(with(any(Instance.class))); will(returnValue(true));
            oneOf(violationFactory).createViolation(with(instance1)); will(returnValue(violation1));
            oneOf(violationFactory).createViolation(with(instance2)); will(returnValue(violation2));
        }});

        MuDetect muDetect = new MuDetect(model, instanceFinder, violationFactory);
        List<Violation> violations = muDetect.findViolations(targets);

        assertThat(violations, containsInAnyOrder(violation1, violation2));
    }
}
