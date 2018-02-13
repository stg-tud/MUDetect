package de.tu_darmstadt.stg.mudetect.overlapsfinder;

import de.tu_darmstadt.stg.mudetect.OverlapsFinder;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.List;

import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.somePattern;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

class OverlapsFinderTestUtils {
    @Deprecated
    static List<Overlap> findOverlaps(TestAUGBuilder patternBuilder, TestAUGBuilder targetBuilder) {
        APIUsageExample target = targetBuilder.build();
        APIUsagePattern pattern = somePattern(patternBuilder);
        return createDefaultFinder().findOverlaps(target, pattern);
    }

    @Deprecated
    static void assertFindsOverlaps(TestAUGBuilder patternBuilder,
                                    TestAUGBuilder targetBuilder,
                                    Overlap... expectedOverlaps) {
        assertThat(createDefaultFinder(), findsOverlaps(targetBuilder, patternBuilder, expectedOverlaps));
    }

    @Deprecated
    static void assertFindsOverlaps(TestAUGBuilder patternBuilder,
                                    TestAUGBuilder targetBuilder,
                                    TestOverlapBuilder... expectedOverlapsBuilders) {
        assertThat(createDefaultFinder(), findsOverlaps(targetBuilder, patternBuilder, expectedOverlapsBuilders));
    }

    private static AlternativeMappingsOverlapsFinder createDefaultFinder() {
        return new AlternativeMappingsOverlapsFinder(new EquallyLabelledNodeMatcher());
    }

    private static Overlap[] buildOverlaps(TestOverlapBuilder[] expectedOverlapsBuilders) {
        Overlap[] expectedOverlaps = new Overlap[expectedOverlapsBuilders.length];
        for (int i = 0; i < expectedOverlapsBuilders.length; i++) {
            expectedOverlaps[i] = expectedOverlapsBuilders[i].build();
        }
        return expectedOverlaps;
    }

    static Matcher<OverlapsFinder> findsOverlaps(TestAUGBuilder targetBuilder, TestAUGBuilder patternBuilder, Overlap... expectedOverlaps) {
        APIUsageExample target = targetBuilder.build();
        APIUsagePattern pattern = somePattern(patternBuilder);
        //noinspection RedundantTypeArguments compiler fails to infer this
        Matcher<List<Overlap>> overlapsMatcher = Matchers.<List<Overlap>>allOf(
                hasSize(expectedOverlaps.length),
                containsInAnyOrder(expectedOverlaps));

        return new BaseMatcher<OverlapsFinder>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof OverlapsFinder) {
                    OverlapsFinder overlapsFinder = (OverlapsFinder) item;
                    List<Overlap> overlaps = overlapsFinder.findOverlaps(target, pattern);
                    return overlapsMatcher.matches(overlaps);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("overlap finder to find ");
                description.appendDescriptionOf(overlapsMatcher);
            }
        };
    }

    static Matcher<OverlapsFinder> findsOverlaps(TestAUGBuilder targetBuilder, TestAUGBuilder patternBuilder, TestOverlapBuilder... expectedOverlapsBuilders) {
        Overlap[] expectedOverlaps = buildOverlaps(expectedOverlapsBuilders);
        return findsOverlaps(targetBuilder, patternBuilder, expectedOverlaps);
    }

    static Matcher<Iterable<? extends Overlap>> contains(TestOverlapBuilder... expectedOverlapsBuilders) {
        Overlap[] expectedOverlaps = buildOverlaps(expectedOverlapsBuilders);
        return Matchers.containsInAnyOrder(expectedOverlaps);
    }
}
