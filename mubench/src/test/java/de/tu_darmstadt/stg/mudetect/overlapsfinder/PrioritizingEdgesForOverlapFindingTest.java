package de.tu_darmstadt.stg.mudetect.overlapsfinder;

import de.tu_darmstadt.stg.mudetect.OverlapsFinder;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import egroum.EGroumDataEdge;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.overlapsfinder.OverlapsFinderTestUtils.findsOverlaps;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static egroum.EGroumDataEdge.Type.RECEIVER;
import static org.junit.Assert.assertThat;

public class PrioritizingEdgesForOverlapFindingTest {
    @Test
    public void prioritizesEdge() throws Exception {
        OverlapsFinder overlapsFinder = new AlternativeMappingsOverlapsFinder(new AlternativeMappingsOverlapsFinder.Config() {{
            nodeMatcher = new EquallyLabelledNodeMatcher();
            edgeOrder = (e1, e2) -> ((EGroumDataEdge) e1).getType() == RECEIVER;
        }});
        TestAUGBuilder target = buildAUG().withActionNode("O.equals()").withDataNode("O")
                .withDataEdge("O", RECEIVER, "O.equals()").withDataEdge("O", PARAMETER, "O.equals()");
        TestAUGBuilder pattern = buildAUG().withActionNode("O.equals()").withDataNode("O1", "O").withDataNode("O2", "O")
                .withDataEdge("O1", RECEIVER, "O.equals()").withDataEdge("O2", PARAMETER, "O.equals()");
        TestOverlapBuilder overlap = buildOverlap(target, pattern).withNode("O.equals()").withNode("O", "O1")
                .withEdge("O", "O1", RECEIVER, "O.equals()", "O.equals()");

        assertThat(overlapsFinder, findsOverlaps(target, pattern, overlap));
    }
}
