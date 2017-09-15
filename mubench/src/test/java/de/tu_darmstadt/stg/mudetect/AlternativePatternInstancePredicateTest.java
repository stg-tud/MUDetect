package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.someOverlap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AlternativePatternInstancePredicateTest {
    @Test
    public void keepsViolation_noInstances() throws Exception {
        final Overlap violation = someOverlap();
        final AlternativePatternInstancePredicate filter = new AlternativePatternInstancePredicate();

        assertFalse(filter.test(violation, Collections.emptyList()));
    }

    @Test
    public void keepsViolation_unrelatedInstances() throws Exception {
        final Overlap violation = someOverlap();
        final Overlap instance1 = someOverlap();
        final Overlap instance2 = someOverlap();
        final AlternativePatternInstancePredicate filter = new AlternativePatternInstancePredicate();

        assertFalse(filter.test(violation, Arrays.asList(instance1, instance2)));
    }

    @Test
    public void keepsViolation_relatedInstance() throws Exception {
        final TestAUGBuilder target = buildAUG().withActionNodes("a", "c");
        final TestAUGBuilder violatedPattern = buildAUG().withActionNodes("a", "b");
        final TestAUGBuilder satisfiedPattern = buildAUG().withActionNodes("a", "c");
        final Overlap violation = buildOverlap(target, violatedPattern).withNode("a", "a").build();
        final Overlap instance = buildOverlap(target, satisfiedPattern).withNode("a", "a").withNode("c", "c").build();
        final AlternativePatternInstancePredicate filter = new AlternativePatternInstancePredicate();

        assertFalse(filter.test(violation, Collections.singleton(instance)));
    }

    @Test
    public void filtersViolation_isInstanceOfOtherPattern() throws Exception {
        final TestAUGBuilder target = buildAUG().withActionNode("a");
        final TestAUGBuilder violatedPattern = buildAUG().withActionNodes("a", "b");
        final TestAUGBuilder satisfiedPattern = buildAUG().withActionNode("a");
        final Overlap violation = buildOverlap(target, violatedPattern).withNode("a", "a").build();
        final Overlap instance = buildOverlap(target, satisfiedPattern).withNode("a", "a").build();
        final AlternativePatternInstancePredicate filter = new AlternativePatternInstancePredicate();

        assertTrue(filter.test(violation, Collections.singleton(instance)));
    }

}
