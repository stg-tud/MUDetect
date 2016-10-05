package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.someInstance;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ViolationFinderTest {
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Mock
    private ViolationFactory violationFactory;

    @Test
    public void findsViolation() throws Exception {
        final Instance instance = someInstance();
        final Instances instances = new Instances(instance);

        context.checking(new Expectations() {{
            allowing(violationFactory).isViolation(with(any(Instance.class))); will(returnValue(true));
        }});

        final ViolationFinder violationFinder = new ViolationFinder(violationFactory);
        List<Violation> violations = violationFinder.findViolations(instances);

        assertThat(violations, hasSize(1));
        assertThat(violations.get(0).getInstance(), is(instance));
    }

    private class ViolationFinder {
        private final ViolationFactory violationFactory;

        public ViolationFinder(ViolationFactory violationFactory) {
            this.violationFactory = violationFactory;
        }

        public List<Violation> findViolations(Instances instances) {
            List<Violation> violations = new ArrayList<>();
            for (Instance instance : instances) {
                violations.add(new Violation(instance, -1));
            }
            return violations;
        }
    }
}
