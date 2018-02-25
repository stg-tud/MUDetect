package de.tu_darmstadt.stg.mudetect.stresstests;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledEdgeMatcher;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.somePattern;
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
    public void skipIfTooMany() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A")
                .withDataNode("B1", "B").withEdge("B1", PARAMETER, "A")
                .withDataNode("B2", "B").withEdge("B2", PARAMETER, "A")
                .withDataNode("B3", "B").withEdge("B3", PARAMETER, "A");
        TestAUGBuilder target = buildAUG().withActionNode("A").withDataNode("B").withEdge("B", PARAMETER, "A");

        AlternativeMappingsOverlapsFinder finder = new AlternativeMappingsOverlapsFinder(
                new AlternativeMappingsOverlapsFinder.Config() {{
                    BaseAUGLabelProvider labelProvider = new BaseAUGLabelProvider();
                    nodeMatcher = new EquallyLabelledNodeMatcher(labelProvider);
                    edgeMatcher = new EquallyLabelledEdgeMatcher(labelProvider);
                    maxNumberOfAlternatives = 2;
                }});
        List<Overlap> overlaps = finder.findOverlaps(target.build(), somePattern(pattern));

        assertThat(overlaps, is(empty()));
    }
}
