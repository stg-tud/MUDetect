package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.function.Predicate;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

public class FilterInstanceTest {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Test
    public void keepsInstance() throws Exception {
        AUG aug = someAUG();
        @SuppressWarnings("unchecked")
        Predicate<Instance> instancePredicate = context.mock(Predicate.class);

        context.checking(new Expectations() {{
            allowing(instancePredicate).test(with(any(Instance.class))); will(returnValue(true));
        }});

        GreedyInstanceFinder finder = new GreedyInstanceFinder(instancePredicate);
        List<Instance> instances = finder.findInstances(aug, aug);

        assertThat(instances, is(not(empty())));
    }

    @Test
    public void filtersInstance() throws Exception {
        AUG aug = someAUG();
        @SuppressWarnings("unchecked")
        Predicate<Instance> instanceFilter = context.mock(Predicate.class);

        context.checking(new Expectations() {{
            allowing(instanceFilter).test(with(any(Instance.class))); will(returnValue(false));
        }});

        GreedyInstanceFinder finder = new GreedyInstanceFinder(instanceFilter);
        List<Instance> instances = finder.findInstances(aug, aug);

        assertThat(instances, is(empty()));
    }
}
