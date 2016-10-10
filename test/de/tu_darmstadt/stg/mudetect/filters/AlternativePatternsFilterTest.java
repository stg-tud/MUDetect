package de.tu_darmstadt.stg.mudetect.filters;

import de.tu_darmstadt.stg.mudetect.Instance;
import de.tu_darmstadt.stg.mudetect.model.Instances;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.buildInstance;
import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.someInstance;
import static org.junit.Assert.assertFalse;

public class AlternativePatternsFilterTest {
    @Test
    public void keepsViolation_noInstances() throws Exception {
        final Instance violation = someInstance();
        final AlternativePatternsFilter filter = new AlternativePatternsFilter();

        assertFalse(filter.test(violation, new Instances()));
    }

    private class AlternativePatternsFilter {
        public boolean test(Instance violation, Instances instances) {
            return false;
        }
    }
}
