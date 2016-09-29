package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class NoOverlapInstanceFinderTest {
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Test
    public void insertsNoOverlapInstanceIfNoOverlap() throws Exception {
        AUG pattern = someAUG();
        AUG target = someAUG();
        InstanceFinder wrappedFinder = context.mock(InstanceFinder.class);
        context.checking(new Expectations() {{
            oneOf(wrappedFinder).findInstances(target, pattern); will(returnValue(Collections.emptyList()));
        }});

        InstanceFinder finder = new NoOverlapInstanceFinder(wrappedFinder);
        List<Instance> instances = finder.findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        Instance noOverlapInstance = new Instance(pattern, target, Collections.emptyMap(), Collections.emptyMap());
        assertThat(instances, hasItems(noOverlapInstance));
    }

    @Test
    public void returnsOriginalInstancesIfAny() throws Exception {
        AUG pattern = someAUG();
        AUG target = someAUG();
        InstanceFinder wrappedFinder = context.mock(InstanceFinder.class);
        Instance instance = new Instance(pattern, target);
        context.checking(new Expectations() {{
            oneOf(wrappedFinder).findInstances(target, pattern); will(returnValue(Collections.singletonList(instance)));
        }});

        InstanceFinder finder = new NoOverlapInstanceFinder(wrappedFinder);
        List<Instance> instances = finder.findInstances(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasItems(instance));
    }

}
