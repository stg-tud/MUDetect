package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.AUGBuilder.someAUG;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MuDetectTest {
    private final JUnitRuleMockery context = new JUnitRuleMockery();

    @Test
    public void findsViolations() throws Exception {
        final Model model = context.mock(Model.class);
        final Pattern pattern = new Pattern(someAUG());
        final AUG target = someAUG();
        final Collection<AUG> targets = Collections.singletonList(target);
        final Instance instance = new Instance(pattern.getAUG(), target);
        final InstanceFinder instanceFinder = context.mock(InstanceFinder.class);
        final ViolationStrategy violationStrategy = context.mock(ViolationStrategy.class);
        context.checking(new Expectations() {{
            allowing(model).getPatterns();
            will(returnValue(Collections.singleton(pattern)));

            allowing(instanceFinder).findInstances(pattern.getAUG(), target);
            will(returnValue(Collections.singletonList(instance)));

            allowing(violationStrategy).isViolation(instance);
            will(returnValue(true));
        }});

        MuDetect muDetect = new MuDetect(model, instanceFinder, violationStrategy);
        List<Violation> violations = muDetect.findViolations(targets);

        assertThat(violations, hasSize(1));
        Violation violation = violations.get(0);
        assertThat(violation.getInstance(), is(instance));
    }

    @Test
    public void ignoresNonViolations() throws Exception {
        final Model model = context.mock(Model.class);
        final Pattern pattern = new Pattern(someAUG());
        final AUG target = someAUG();
        final Collection<AUG> targets = Collections.singletonList(target);
        final Instance instance = new Instance(pattern.getAUG(), target);
        final InstanceFinder instanceFinder = context.mock(InstanceFinder.class);
        final ViolationStrategy violationStrategy = context.mock(ViolationStrategy.class);
        context.checking(new Expectations() {{
            allowing(model).getPatterns();
            will(returnValue(Collections.singleton(pattern)));

            allowing(instanceFinder).findInstances(pattern.getAUG(), target);
            will(returnValue(Collections.singletonList(instance)));

            allowing(violationStrategy).isViolation(instance);
            will(returnValue(false));
        }});

        MuDetect muDetect = new MuDetect(model, instanceFinder, violationStrategy);
        List<Violation> violations = muDetect.findViolations(targets);

        assertThat(violations, is(empty()));
    }

}
