package de.tu_darmstadt.stg.mudetect.filters;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Instances;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.extend;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class SmallOverlapsFilterTest {
    @Test
    public void keepsIfOverlapIsLarge() throws Exception {
        final TestAUGBuilder builder = buildAUG().withActionNodes("a", "b");
        final Instance instance = buildInstance(builder, builder).withNode("a", "a").withNode("b", "b").build();
        final SmallOverlapFilter filter = new SmallOverlapFilter(0.5);

        assertTrue(filter.test(instance, null));
    }

    @Test
    public void filtersIfOverlapIsSmall() throws Exception {
        final TestAUGBuilder targetBuilder = buildAUG().withActionNodes("a");
        final TestAUGBuilder patternBuilder = extend(targetBuilder).withActionNodes("b", "c");
        final Instance instance = buildInstance(targetBuilder, patternBuilder).withNode("a", "a").build();
        final SmallOverlapFilter filter = new SmallOverlapFilter(0.5);

        assertFalse(filter.test(instance, null));
    }

    private class SmallOverlapFilter implements InstanceFilter {
        private final double overlapRatioThreshold;

        public SmallOverlapFilter(double overlapRatioThreshold) {
            this.overlapRatioThreshold = overlapRatioThreshold;
        }

        @Override
        public boolean test(Instance instance, Instances instances) {
            return instance.getNodeSize() / (float) instance.getPattern().getNodeSize() > overlapRatioThreshold;
        }
    }
}
