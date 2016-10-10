package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.*;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.someInstance;
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
        final Instance instance = someInstance(pattern.getAUG(), target);
        final Violation violation = new Violation(instance, 1);

        context.checking(new Expectations() {{
            oneOf(model).getPatterns(); will(returnValue(Collections.singleton(pattern)));
            oneOf(instanceFinder).findInstances(target, pattern.getAUG()); will(returnValue(Collections.singletonList(instance)));
            oneOf(violationFactory).isViolation(instance); will(returnValue(true));
            oneOf(violationFactory).createViolation(instance); will(returnValue(violation));
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
        final Instance instance = someInstance(pattern.getAUG(), target);

        context.checking(new Expectations() {{
            oneOf(model).getPatterns(); will(returnValue(Collections.singleton(pattern)));
            oneOf(instanceFinder).findInstances(target, pattern.getAUG()); will(returnValue(Collections.singletonList(instance)));
            allowing(violationFactory).isViolation(with(any(Instance.class))); will(returnValue(false));
        }});

        MuDetect muDetect = new MuDetect(model, instanceFinder, violationFactory);
        List<Violation> violations = muDetect.findViolations(targets);

        assertThat(violations, is(empty()));
    }

    @Test
    public void ranksViolations() throws Exception {
        TestAUGBuilder builder = buildAUG().withActionNodes("a", "b");
        final AUG pattern = builder.build();
        final AUG target = builder.build();
        final Collection<AUG> targets = Collections.singletonList(target);
        final Instance instance1 = buildInstance(builder, builder).withNode("a", "a").build();
        final Violation violation1 = new Violation(instance1, 0.9f);
        final Instance instance2 = buildInstance(builder, builder).withNode("b", "b").build();
        final Violation violation2 = new Violation(instance2, 0.7f);

        context.checking(new Expectations() {{
            oneOf(model).getPatterns(); will(returnValue(Collections.singleton(somePattern(pattern))));
            oneOf(instanceFinder).findInstances(target, pattern); will(returnValue(Arrays.asList(instance2, instance1)));
            allowing(violationFactory).isViolation(instance1); will(returnValue(true));
            oneOf(violationFactory).createViolation(with(instance1)); will(returnValue(violation1));
            allowing(violationFactory).isViolation(instance2); will(returnValue(true));
            oneOf(violationFactory).createViolation(with(instance2)); will(returnValue(violation2));
        }});

        MuDetect muDetect = new MuDetect(model, instanceFinder, violationFactory);
        List<Violation> violations = muDetect.findViolations(targets);

        assertThat(violations, contains(violation1, violation2));
    }

    @Test
    public void ranksViolationsWithSameConfidenceInAnyOrder() throws Exception {
        TestAUGBuilder builder = buildAUG().withActionNodes("a", "b");
        final AUG pattern = builder.build();
        final AUG target = builder.build();
        final Collection<AUG> targets = Collections.singletonList(target);
        final Instance instance1 = buildInstance(builder, builder).withNode("a", "a").build();
        final Violation violation1 = new Violation(instance1, 0.9f);
        final Instance instance2 = buildInstance(builder, builder).withNode("b", "b").build();
        final Violation violation2 = new Violation(instance2, 0.7f);

        context.checking(new Expectations() {{
            oneOf(model).getPatterns(); will(returnValue(Collections.singleton(somePattern(pattern))));
            oneOf(instanceFinder).findInstances(target, pattern); will(returnValue(Arrays.asList(instance2, instance1)));
            allowing(violationFactory).isViolation(instance1); will(returnValue(true));
            oneOf(violationFactory).createViolation(with(instance1)); will(returnValue(violation1));
            allowing(violationFactory).isViolation(instance2); will(returnValue(true));
            oneOf(violationFactory).createViolation(with(instance2)); will(returnValue(violation2));
        }});

        MuDetect muDetect = new MuDetect(model, instanceFinder, violationFactory);
        List<Violation> violations = muDetect.findViolations(targets);

        assertThat(violations, containsInAnyOrder(violation1, violation2));
    }
}
