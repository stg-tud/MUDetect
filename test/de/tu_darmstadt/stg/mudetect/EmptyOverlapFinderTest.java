package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.someOverlap;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class EmptyOverlapFinderTest {
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Test
    public void insertsEmptyOverlapIfNoOverlaps() throws Exception {
        Pattern pattern = somePattern();
        AUG target = someAUG();
        OverlapsFinder wrappedFinder = context.mock(OverlapsFinder.class);
        context.checking(new Expectations() {{
            oneOf(wrappedFinder).findOverlaps(target, pattern); will(returnValue(Collections.emptyList()));
        }});

        OverlapsFinder finder = new EmptyOverlapsFinder(wrappedFinder);
        List<Overlap> instances = finder.findOverlaps(target, pattern);

        assertThat(instances, hasSize(1));
        Overlap noOverlapInstance = new Overlap(pattern, target, Collections.emptyMap(), Collections.emptyMap());
        assertThat(instances, hasItems(noOverlapInstance));
    }

    @Test
    public void returnsOriginalOverlapsIfAny() throws Exception {
        Overlap overlap = someOverlap();
        Pattern pattern = overlap.getPattern();
        AUG target = overlap.getTarget();
        OverlapsFinder wrappedFinder = context.mock(OverlapsFinder.class);
        context.checking(new Expectations() {{
            oneOf(wrappedFinder).findOverlaps(target, pattern); will(returnValue(Collections.singletonList(overlap)));
        }});

        OverlapsFinder finder = new EmptyOverlapsFinder(wrappedFinder);
        List<Overlap> instances = finder.findOverlaps(target, pattern);

        assertThat(instances, hasSize(1));
        assertThat(instances, hasItems(overlap));
    }

}
