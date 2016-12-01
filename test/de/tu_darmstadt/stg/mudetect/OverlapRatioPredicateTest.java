package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.extend;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class OverlapRatioPredicateTest {
    @Test
    public void keepsIfOverlapIsLarge() throws Exception {
        final TestAUGBuilder builder = buildAUG().withActionNodes("a", "b");
        final Overlap overlap = buildOverlap(builder, builder).withNode("a", "a").withNode("b", "b").build();
        final OverlapRatioPredicate filter = new OverlapRatioPredicate(0.5);

        assertTrue(filter.test(overlap));
    }

    @Test
    public void filtersIfOverlapIsSmall() throws Exception {
        final TestAUGBuilder targetBuilder = buildAUG().withActionNodes("a");
        final TestAUGBuilder patternBuilder = extend(targetBuilder).withActionNodes("b", "c");
        final Overlap overlap = buildOverlap(targetBuilder, patternBuilder).withNode("a", "a").build();
        final OverlapRatioPredicate filter = new OverlapRatioPredicate(0.5);

        assertFalse(filter.test(overlap));
    }

}
