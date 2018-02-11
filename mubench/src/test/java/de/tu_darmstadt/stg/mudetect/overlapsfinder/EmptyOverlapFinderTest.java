package de.tu_darmstadt.stg.mudetect.overlapsfinder;

import de.tu_darmstadt.stg.mudetect.OverlapsFinder;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.someAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.someOverlap;
import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.somePattern;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class EmptyOverlapFinderTest {
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Test
    public void insertsEmptyOverlapIfNoOverlaps() {
        APIUsagePattern pattern = somePattern();
        APIUsageExample target = someAUG();
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
    public void returnsOriginalOverlapsIfAny() {
        Overlap overlap = someOverlap();
        APIUsagePattern pattern = overlap.getPattern();
        APIUsageExample target = overlap.getTarget();
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
