package de.tu_darmstadt.stg.mudetect.filters;

import de.tu_darmstadt.stg.mudetect.Instance;
import de.tu_darmstadt.stg.mudetect.model.Instances;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static junit.framework.TestCase.assertTrue;

public class SmallOverlapsFilterTest {
    @Test
    public void keepsIfOverlapIsLarge() throws Exception {
        final TestAUGBuilder builder = buildAUG().withActionNodes("a", "b");
        final Instance instance = buildInstance(builder, builder).withNode("a", "a").withNode("b", "b").build();
        final SmallOverlapFilter filter = new SmallOverlapFilter(0.5);

        assertTrue(filter.test(instance, null));
    }

    private class SmallOverlapFilter implements InstanceFilter {
        public SmallOverlapFilter(double overlapRatioThreshold) {

        }

        @Override
        public boolean test(Instance instance, Instances instances) {
            return true;
        }
    }
}
