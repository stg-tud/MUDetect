package de.tu_darmstadt.stg.mudetect.stresstests;

import de.tu_darmstadt.stg.mudetect.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.mining.TestPatternBuilder.somePattern;
import static egroum.EGroumDataEdge.Type.ORDER;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * To prevent our detection from running out of memory when there's too many alternatives, we set an upper bound to
 * the number of alternatives that we explore. If the detection reaches this bound, it skips detection for the current
 * pair of pattern and target.
 */
public class LimitExploredAlternativesTest {
    @Test
    public void skipIfTooMany() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A").withDataNode("B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNode("A")
                .withDataNode("B1", "B").withDataEdge("A", ORDER, "B1")
                .withDataNode("B2", "B").withDataEdge("A", ORDER, "B2")
                .withDataNode("B3", "B").withDataEdge("A", ORDER, "B3");

        AlternativeMappingsOverlapsFinder finder = new AlternativeMappingsOverlapsFinder();
        finder.setMaxNumberOfAlternatives(2);
        List<Overlap> overlaps = finder.findOverlaps(target.build(), somePattern(pattern));

        assertThat(overlaps, is(empty()));
    }
}
