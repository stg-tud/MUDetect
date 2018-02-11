package edu.iastate.cs.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import org.junit.Test;

import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.*;
import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.somePattern;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static de.tu_darmstadt.stg.mudetect.utils.SetUtils.asSet;

public class ModelTest {
    @Test
    public void computesMaxSupportBySize() throws Exception {
        APIUsagePattern pattern1 = somePattern(3, 5);
        APIUsagePattern pattern2 = somePattern(3, 23);
        Model model = () -> asSet(pattern1, pattern2);

        int maxPatternSupport = model.getMaxPatternSupport(3);

        assertThat(maxPatternSupport, is(23));
    }

    @Test
    public void computesMaxSupportOnPatternsWithSameNodeCount() throws Exception {
        APIUsagePattern pattern1 = somePattern(3, 23);
        APIUsagePattern pattern2 = somePattern(5, 42);
        Model model = () -> asSet(pattern1, pattern2);

        int maxPatternSupport = model.getMaxPatternSupport(3);

        assertThat(maxPatternSupport, is(23));
    }

    @Test
    public void computesMaxSupport() throws Exception {
        Model model = () -> asSet(somePattern(5), somePattern(42), somePattern(23));

        assertThat(model.getMaxPatternSupport(), is(42));
    }
}
