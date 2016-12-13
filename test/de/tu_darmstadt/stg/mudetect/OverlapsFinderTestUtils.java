package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.*;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

class OverlapsFinderTestUtils {
    static List<Overlap> findOverlaps(TestAUGBuilder patternBuilder, TestAUGBuilder targetBuilder) {
        AUG target = targetBuilder.build();
        Pattern pattern = somePattern(patternBuilder);

        return new AlternativeMappingsOverlapsFinder().findOverlaps(target, pattern);
    }

    static void assertFindsOverlaps(TestAUGBuilder patternBuilder,
                                    TestAUGBuilder targetBuilder,
                                    Overlap... expectedOverlaps) {
        List<Overlap> overlaps = findOverlaps(patternBuilder, targetBuilder);

        assertThat(overlaps, hasSize(expectedOverlaps.length));
        assertThat(overlaps, containsInAnyOrder(expectedOverlaps));
    }

    static void assertFindsOverlaps(TestAUGBuilder patternBuilder,
                                    TestAUGBuilder targetBuilder,
                                    TestOverlapBuilder... expectedOverlapsBuilders) {
        Overlap[] expectedOverlaps = new Overlap[expectedOverlapsBuilders.length];
        for (int i = 0; i < expectedOverlapsBuilders.length; i++) {
            expectedOverlaps[i] = expectedOverlapsBuilders[i].build();
        }

        assertFindsOverlaps(patternBuilder, targetBuilder, expectedOverlaps);
    }
}
