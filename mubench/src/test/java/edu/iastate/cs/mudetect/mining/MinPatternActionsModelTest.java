package edu.iastate.cs.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import org.junit.Test;

import java.util.Set;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.SYNCHRONIZE;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.THROW;
import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.somePattern;
import static de.tu_darmstadt.stg.mudetect.utils.SetUtils.asSet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MinPatternActionsModelTest {
    @Test
    public void filtersPatternWithFewerActions() {
        APIUsagePattern pattern = somePattern(TestAUGBuilder.buildAUG().withActionNode("m()"));

        Set<APIUsagePattern> patterns = new MinPatternActionsModel(() -> asSet(pattern), 2).getPatterns();

        assertThat(patterns, is(empty()));
    }

    @Test
    public void considersCalls() {
        APIUsagePattern pattern = somePattern(TestAUGBuilder.buildAUG().withActionNodes("m()", "n()"));

        Set<APIUsagePattern> patterns = new MinPatternActionsModel(() -> asSet(pattern), 2).getPatterns();

        assertThat(patterns, is(not(empty())));
    }

    @Test
    public void considersCatch() {
        APIUsagePattern pattern = somePattern(TestAUGBuilder.buildAUG()
                .withActionNodes("m()").withDataNode("SomeException")
                .withEdge("m()", THROW, "SomeException"));

        Set<APIUsagePattern> patterns = new MinPatternActionsModel(() -> asSet(pattern), 2).getPatterns();

        assertThat(patterns, is(not(empty())));
    }

    @Test
    public void considersSynchronization() {
        APIUsagePattern pattern = somePattern(TestAUGBuilder.buildAUG()
                .withDataNode("Object").withActionNodes("m()")
                .withEdge("Object", SYNCHRONIZE, "m()"));

        Set<APIUsagePattern> patterns = new MinPatternActionsModel(() -> asSet(pattern), 2).getPatterns();

        assertThat(patterns, is(not(empty())));
    }
}
