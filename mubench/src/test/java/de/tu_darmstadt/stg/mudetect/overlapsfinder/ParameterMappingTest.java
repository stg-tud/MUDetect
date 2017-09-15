package de.tu_darmstadt.stg.mudetect.overlapsfinder;

import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.mining.TestPatternBuilder.somePattern;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.overlapsfinder.OverlapsFinderTestUtils.contains;
import static egroum.EGroumDataEdge.Type.DEFINITION;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static org.junit.Assert.assertThat;

public class ParameterMappingTest {
    private AlternativeMappingsOverlapsFinder overlapsFinder;

    @Before
    public void setUp() throws Exception {
        overlapsFinder = new AlternativeMappingsOverlapsFinder(new AlternativeMappingsOverlapsFinder.Config() {{
            nodeMatcher = new EquallyLabelledNodeMatcher();
        }});
    }

    @Test
    public void mapsParameter() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNode("m()").withDataNode("P").withDataEdge("P", PARAMETER, "m()");
        TestAUGBuilder target = buildAUG().withActionNode("m()").withDataNode("P").withDataEdge("P", PARAMETER, "m()");

        List<Overlap> overlaps = overlapsFinder.findOverlaps(target.build(), somePattern(pattern));

        TestOverlapBuilder overlap = buildOverlap(target, pattern).withNodes("P", "m()").withEdge("P", PARAMETER, "m()");
        assertThat(overlaps, contains(overlap));
    }

    @Test
    public void mapsParameterWithSource() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("source()", "m()").withDataNode("P")
                .withDataEdge("source()", DEFINITION, "P").withDataEdge("P", PARAMETER, "m()");
        TestAUGBuilder target = buildAUG().withActionNodes("source()", "m()").withDataNode("P")
                .withDataEdge("source()", DEFINITION, "P").withDataEdge("P", PARAMETER, "m()");

        List<Overlap> overlaps = overlapsFinder.findOverlaps(target.build(), somePattern(pattern));

        TestOverlapBuilder overlap = buildOverlap(target, pattern).withNodes("source()", "P", "m()")
                .withEdge("source()", DEFINITION, "P").withEdge("P", PARAMETER, "m()");
        assertThat(overlaps, contains(overlap));
    }

    @Test
    public void mapsParameterWithMissingSource() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("source()", "m()").withDataNode("P")
                .withDataEdge("source()", DEFINITION, "P").withDataEdge("P", PARAMETER, "m()");
        TestAUGBuilder target = buildAUG().withActionNode("m()").withDataNode("P")
                .withDataEdge("P", PARAMETER, "m()");

        List<Overlap> overlaps = overlapsFinder.findOverlaps(target.build(), somePattern(pattern));

        TestOverlapBuilder overlap = buildOverlap(target, pattern).withNodes("P", "m()").withEdge("P", PARAMETER, "m()");
        assertThat(overlaps, contains(overlap));
    }

    /**
     * When the target has more alternatives than the pattern, the greedy strategy explores only one of them. To reduce
     * the number of false positives, we ensure that corresponding direct and indirect edges always get mapped together.
     */
    @Test
    public void mapsCorrespondingDirectAndIndirectEdges() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("src()", "sink()").withDataEdge("src()", PARAMETER, "sink()")
                .withDataNode("P1", "P").withDataEdge("P1", PARAMETER, "sink()")
                .withDataNode("P2", "P").withDataEdge("src()", DEFINITION, "P2").withDataEdge("P2", PARAMETER, "sink()");
        // From the four direct edges in this target, the two para edges have two alternative mappings each, while
        // the two def edges have one alternative mapping each, hence, they get mapped first. If the def edge to P3 is
        // mapped first, P2 -(para)-> sink() cannot be mapped anymore, hence, we find a false-positive violation. To
        // avoid this, we give priority to the corresponding direct para edge after mapping the indirect para edge.
        TestAUGBuilder target = buildAUG().withActionNodes("src()", "sink()").withDataEdge("src()", PARAMETER, "sink()")
                .withDataNode("P1", "P").withDataEdge("P1", PARAMETER, "sink()")
                .withDataNode("P3", "P").withDataEdge("src()", DEFINITION, "P3")
                .withDataNode("P2", "P").withDataEdge("src()", DEFINITION, "P2").withDataEdge("P2", PARAMETER, "sink()");

        List<Overlap> overlaps = overlapsFinder.findOverlaps(target.build(), somePattern(pattern));

        TestOverlapBuilder overlap = buildOverlap(target, pattern)
                .withNodes("src()", "sink()").withEdge("src()", PARAMETER, "sink()")
                .withNode("P1").withEdge("P1", PARAMETER, "sink()")
                .withNode("P2").withEdge("src()", DEFINITION, "P2").withEdge("P2", PARAMETER, "sink()");
        assertThat(overlaps, contains(overlap));
    }
}
