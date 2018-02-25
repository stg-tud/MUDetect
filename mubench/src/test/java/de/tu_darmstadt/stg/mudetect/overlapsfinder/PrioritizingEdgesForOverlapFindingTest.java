package de.tu_darmstadt.stg.mudetect.overlapsfinder;

import de.tu_darmstadt.stg.mudetect.OverlapsFinder;
import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledEdgeMatcher;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.RECEIVER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.overlapsfinder.OverlapsFinderTestUtils.findsOverlaps;
import static org.junit.Assert.assertThat;

public class PrioritizingEdgesForOverlapFindingTest {
    @Test
    public void prioritizesEdge() {
        OverlapsFinder overlapsFinder = new AlternativeMappingsOverlapsFinder(
                new AlternativeMappingsOverlapsFinder.Config() {{
                    BaseAUGLabelProvider labelProvider = new BaseAUGLabelProvider();
                    nodeMatcher = new EquallyLabelledNodeMatcher(labelProvider);
                    edgeMatcher = new EquallyLabelledEdgeMatcher(labelProvider);
                    edgeOrder = (e1, e2) -> e1.getType() == RECEIVER;
                }});
        TestAUGBuilder target = buildAUG().withActionNode("O.equals()").withDataNode("O")
                .withEdge("O", RECEIVER, "O.equals()").withEdge("O", PARAMETER, "O.equals()");
        TestAUGBuilder pattern = buildAUG().withActionNode("O.equals()").withDataNode("O1", "O").withDataNode("O2", "O")
                .withEdge("O1", RECEIVER, "O.equals()").withEdge("O2", PARAMETER, "O.equals()");
        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNode("O.equals()").withNode("O", "O1")
                .withEdge("O", "O1", RECEIVER, "O.equals()", "O.equals()");

        assertThat(overlapsFinder, findsOverlaps(target, pattern, overlap));
    }
}
