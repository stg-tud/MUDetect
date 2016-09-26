package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

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

    @Test
    public void findsViolations() throws Exception {
        final Pattern pattern = somePattern();
        final AUG target = someAUG();
        final Collection<AUG> targets = Collections.singletonList(target);
        final Instance instance = new Instance(pattern.getAUG(), target);
        final Violation violation = new Violation(instance);

        final Model model = context.mock(Model.class);
        final InstanceFinder instanceFinder = context.mock(InstanceFinder.class);
        final ViolationFactory violationFactory = context.mock(ViolationFactory.class);

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

        final Model model = context.mock(Model.class);
        final InstanceFinder instanceFinder = context.mock(InstanceFinder.class);
        final ViolationFactory violationFactory = context.mock(ViolationFactory.class);

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

}
