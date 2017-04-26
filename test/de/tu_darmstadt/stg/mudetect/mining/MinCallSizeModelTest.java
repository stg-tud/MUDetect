package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import java.util.Set;

import static de.tu_darmstadt.stg.mudetect.mining.TestPatternBuilder.somePattern;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
import static utils.SetUtils.asSet;

public class MinCallSizeModelTest {
    @Test
    public void filtersPatternWithFewerCalls() throws Exception {
        Pattern pattern = somePattern(TestAUGBuilder.buildAUG().withActionNode("m()"));

        Set<Pattern> patterns = new MinCallSizeModel(() -> asSet(pattern), 2).getPatterns();

        assertThat(patterns, is(empty()));
    }

    @Test
    public void keepsPatternWithMoreCalls() throws Exception {
        Pattern pattern = somePattern(TestAUGBuilder.buildAUG().withActionNodes("m()", "n()"));

        Set<Pattern> patterns = new MinCallSizeModel(() -> asSet(pattern), 2).getPatterns();

        assertThat(patterns, is(not(empty())));
    }
}