package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instances;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.someInstance;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ViolationFinderTest {
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Test
    public void createsViolations() throws Exception {
        final Instance instance = someInstance();
        final Instances instances = new Instances(instance);

        final ViolationFinder violationFinder = new ViolationFinder();
        List<Violation> violations = violationFinder.findViolations(instances);

        assertThat(violations, hasSize(1));
        assertThat(violations.get(0).getInstance(), is(instance));
    }

    @Test
    public void filtersInstanceByPredicate() throws Exception {
        final Instance instance1 = someInstance();
        final Instance instance2 = someInstance();
        final Instances instances = new Instances(instance1, instance2);
        InstancePredicate instancePredicate = context.mock(InstancePredicate.class);

        context.checking(new Expectations() {{
            allowing(instancePredicate).test(instance1, instances); will(returnValue(false));
            allowing(instancePredicate).test(instance2, instances); will(returnValue(true));
        }});

        final ViolationFinder violationFinder = new ViolationFinder(instancePredicate);
        List<Violation> violations = violationFinder.findViolations(instances);

        assertThat(violations, hasSize(1));
        assertThat(violations.get(0).getInstance(), is(instance2));
    }

    @Test
    public void filtersInstanceThatMatchesAnyPredicate() throws Exception {
        final Instance instance = someInstance();
        final Instances instances = new Instances(instance);
        InstancePredicate predicate1 = context.mock(InstancePredicate.class, "predicate 1");
        InstancePredicate predicate2 = context.mock(InstancePredicate.class, "predicate 2");

        context.checking(new Expectations() {{
            allowing(predicate1).and(predicate2);
            allowing(predicate1).test(instance, instances); will(returnValue(false));
            allowing(predicate2).test(instance, instances); will(returnValue(true));
        }});

        final ViolationFinder violationFinder = new ViolationFinder(predicate1, predicate2);
        final List<Violation> violations = violationFinder.findViolations(instances);

        assertThat(violations, is(empty()));
    }

    private interface InstancePredicate extends BiPredicate<Instance, Instances> {}

    private class ViolationFinder {
        private final BiPredicate<Instance, Instances> predicate;

        public ViolationFinder(InstancePredicate... instancePredicates) {
            predicate = Arrays.<BiPredicate<Instance, Instances>>stream(instancePredicates)
                    .reduce(BiPredicate::and)
                    .orElse((i, is) -> true);
        }

        public List<Violation> findViolations(Instances instances) {
            return StreamSupport.stream(instances.spliterator(), false)
                    .filter(instance -> satisfiesAllInstancePredicates(instance, instances))
                    .map(instance -> new Violation(instance, -1))
                    .collect(Collectors.toList());
        }

        private boolean satisfiesAllInstancePredicates(Instance instance, Instances instances) {

            return predicate.test(instance, instances);
        }
    }
}
