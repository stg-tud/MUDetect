package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder.somePattern;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class FindNoOverlapsTest {
    @Test
    public void ignoresNonOverlappingTarget() throws Exception {
        Pattern pattern = somePattern(buildAUG().withActionNode("F.foo()").build());
        AUG target = buildAUG().withActionNode("B.bar()").build();

        assertNoOverlaps(pattern, target);
    }

    @Test
    public void ignoresDateNodeOverlap() throws Exception {
        Pattern pattern = somePattern(buildAUG().withDataNode("Object").build());
        AUG target = buildAUG().withDataNode("Object").build();

        assertNoOverlaps(pattern, target);
    }

    private void assertNoOverlaps(Pattern pattern, AUG target) {
        List<Overlap> overlaps = new AlternativeMappingsOverlapsFinder().findOverlaps(target, pattern);

        assertThat(overlaps, is(empty()));
    }
}
