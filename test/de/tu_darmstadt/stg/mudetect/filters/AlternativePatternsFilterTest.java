package de.tu_darmstadt.stg.mudetect.filters;

import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Instances;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.someInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AlternativePatternsFilterTest {
    @Test
    public void keepsViolation_noInstances() throws Exception {
        final Instance violation = someInstance();
        final AlternativePatternsFilter filter = new AlternativePatternsFilter();

        assertFalse(filter.test(violation, new Instances()));
    }

    @Test
    public void keepsViolation_unrelatedInstances() throws Exception {
        final Instance violation = someInstance();
        final Instance instance1 = someInstance();
        final Instance instance2 = someInstance();
        final AlternativePatternsFilter filter = new AlternativePatternsFilter();

        assertFalse(filter.test(violation, new Instances(instance1, instance2)));
    }

    @Test
    public void keepsViolation_relatedInstance() throws Exception {
        final TestAUGBuilder target = buildAUG().withActionNodes("a", "c");
        final TestAUGBuilder violatedPattern = buildAUG().withActionNodes("a", "b");
        final TestAUGBuilder satisfiedPattern = buildAUG().withActionNodes("a", "c");
        final Instance violation = buildInstance(target, violatedPattern).withNode("a", "a").build();
        final Instance instance = buildInstance(target, satisfiedPattern).withNode("a", "a").withNode("c", "c").build();
        final AlternativePatternsFilter filter = new AlternativePatternsFilter();

        assertFalse(filter.test(violation, new Instances(instance)));
    }

    @Test
    public void filtersViolation_isInstanceOfOtherPattern() throws Exception {
        final TestAUGBuilder target = buildAUG().withActionNode("a");
        final TestAUGBuilder violatedPattern = buildAUG().withActionNodes("a", "b");
        final TestAUGBuilder satisfiedPattern = buildAUG().withActionNode("a");
        final Instance violation = buildInstance(target, violatedPattern).withNode("a", "a").build();
        final Instance instance = buildInstance(target, satisfiedPattern).withNode("a", "a").build();
        final AlternativePatternsFilter filter = new AlternativePatternsFilter();

        assertTrue(filter.test(violation, new Instances(instance)));
    }

    private class AlternativePatternsFilter {
        public boolean test(Instance violation, Instances instances) {
            for (Instance instance : instances) {
                if (violation.isSameTargetOverlap(instance)) {
                    return true;
                }
            }
            return false;
        }
    }
}
