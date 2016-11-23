package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.extend;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class OverlapRatioPredicateTest {
    @Test
    public void keepsIfOverlapIsLarge() throws Exception {
        final TestAUGBuilder builder = buildAUG().withActionNodes("a", "b");
        final Instance instance = buildInstance(builder, builder).withNode("a", "a").withNode("b", "b").build();
        final OverlapRatioPredicate filter = new OverlapRatioPredicate(0.5);

        assertTrue(filter.test(instance));
    }

    @Test
    public void filtersIfOverlapIsSmall() throws Exception {
        final TestAUGBuilder targetBuilder = buildAUG().withActionNodes("a");
        final TestAUGBuilder patternBuilder = extend(targetBuilder).withActionNodes("b", "c");
        final Instance instance = buildInstance(targetBuilder, patternBuilder).withNode("a", "a").build();
        final OverlapRatioPredicate filter = new OverlapRatioPredicate(0.5);

        assertFalse(filter.test(instance));
    }

}
