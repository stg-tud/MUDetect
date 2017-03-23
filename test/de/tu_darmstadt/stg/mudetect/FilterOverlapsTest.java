package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.mining.Pattern;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.function.Predicate;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;
import static de.tu_darmstadt.stg.mudetect.mining.TestPatternBuilder.somePattern;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

public class FilterOverlapsTest {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Test
    public void keepsInstance() throws Exception {
        AUG target = someAUG();
        Pattern pattern = somePattern(target);
        @SuppressWarnings("unchecked")
        Predicate<Overlap> overlapPredicate = context.mock(Predicate.class);

        context.checking(new Expectations() {{
            allowing(overlapPredicate).test(with(any(Overlap.class))); will(returnValue(true));
        }});

        OverlapsFinder finder = new AlternativeMappingsOverlapsFinder(overlapPredicate);
        List<Overlap> overlaps = finder.findOverlaps(target, pattern);

        assertThat(overlaps, is(not(empty())));
    }

    @Test
    public void filtersInstance() throws Exception {
        AUG target = someAUG();
        Pattern pattern = somePattern(target);
        @SuppressWarnings("unchecked")
        Predicate<Overlap> overlapPredicate = context.mock(Predicate.class);

        context.checking(new Expectations() {{
            allowing(overlapPredicate).test(with(any(Overlap.class))); will(returnValue(false));
        }});

        OverlapsFinder finder = new AlternativeMappingsOverlapsFinder(overlapPredicate);
        List<Overlap> overlaps = finder.findOverlaps(target, pattern);

        assertThat(overlaps, is(empty()));
    }
}
