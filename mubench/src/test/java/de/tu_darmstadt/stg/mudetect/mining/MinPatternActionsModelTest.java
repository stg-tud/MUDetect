package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import java.util.Set;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.SYNCHRONIZE;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.THROW;
import static de.tu_darmstadt.stg.mudetect.mining.TestPatternBuilder.somePattern;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
import static de.tu_darmstadt.stg.mudetect.utils.SetUtils.asSet;

public class MinPatternActionsModelTest {
    @Test
    public void filtersPatternWithFewerActions() throws Exception {
        APIUsagePattern pattern = somePattern(TestAUGBuilder.buildAUG().withActionNode("m()"));

        Set<APIUsagePattern> patterns = new MinPatternActionsModel(() -> asSet(pattern), 2).getPatterns();

        assertThat(patterns, is(empty()));
    }

    @Test
    public void considersCalls() throws Exception {
        APIUsagePattern pattern = somePattern(TestAUGBuilder.buildAUG().withActionNodes("m()", "n()"));

        Set<APIUsagePattern> patterns = new MinPatternActionsModel(() -> asSet(pattern), 2).getPatterns();

        assertThat(patterns, is(not(empty())));
    }

    @Test
    public void considersCatch() throws Exception {
        APIUsagePattern pattern = somePattern(TestAUGBuilder.buildAUG()
                .withActionNodes("m()").withDataNode("SomeException")
                .withDataEdge("m()", THROW, "SomeException"));

        Set<APIUsagePattern> patterns = new MinPatternActionsModel(() -> asSet(pattern), 2).getPatterns();

        assertThat(patterns, is(not(empty())));
    }

    @Test
    public void considersSynchronization() throws Exception {
        APIUsagePattern pattern = somePattern(TestAUGBuilder.buildAUG()
                .withDataNode("Object").withActionNodes("m()")
                .withDataEdge("Object", SYNCHRONIZE, "m()"));

        Set<APIUsagePattern> patterns = new MinPatternActionsModel(() -> asSet(pattern), 2).getPatterns();

        assertThat(patterns, is(not(empty())));
    }
}
