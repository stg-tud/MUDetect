package de.tu_darmstadt.stg.mudetect.overlapsfinder;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledEdgeMatcher;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.*;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.SELECTION;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.overlapsfinder.OverlapsFinderTestUtils.contains;
import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.somePattern;
import static org.junit.Assert.assertThat;

public class ParameterMappingTest {
    private AlternativeMappingsOverlapsFinder overlapsFinder;

    @Before
    public void setUp() {
        overlapsFinder = new AlternativeMappingsOverlapsFinder(
                new AlternativeMappingsOverlapsFinder.Config() {{
                    BaseAUGLabelProvider labelProvider = new BaseAUGLabelProvider();
                    nodeMatcher = new EquallyLabelledNodeMatcher(labelProvider);
                    edgeMatcher = new EquallyLabelledEdgeMatcher(labelProvider);
                }});
    }

    @Test
    public void mapsParameter() {
        TestAUGBuilder pattern = buildAUG().withActionNode("m()").withDataNode("P").withEdge("P", PARAMETER, "m()");
        TestAUGBuilder target = buildAUG().withActionNode("m()").withDataNode("P").withEdge("P", PARAMETER, "m()");

        List<Overlap> overlaps = overlapsFinder.findOverlaps(target.build(), somePattern(pattern));

        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("P", "m()").withEdge("P", PARAMETER, "m()");
        assertThat(overlaps, contains(overlap));
    }

    @Test
    public void mapsParameterWithSource() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("source()", "m()").withDataNode("P")
                .withEdge("source()", DEFINITION, "P").withEdge("P", PARAMETER, "m()");
        TestAUGBuilder target = buildAUG().withActionNodes("source()", "m()").withDataNode("P")
                .withEdge("source()", DEFINITION, "P").withEdge("P", PARAMETER, "m()");

        List<Overlap> overlaps = overlapsFinder.findOverlaps(target.build(), somePattern(pattern));

        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("source()", "P", "m()")
                .withEdge("source()", DEFINITION, "P").withEdge("P", PARAMETER, "m()");
        assertThat(overlaps, contains(overlap));
    }

    @Test
    public void mapsParameterWithMissingSource() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("source()", "m()").withDataNode("P")
                .withEdge("source()", DEFINITION, "P").withEdge("P", PARAMETER, "m()");
        TestAUGBuilder target = buildAUG().withActionNode("m()").withDataNode("P")
                .withEdge("P", PARAMETER, "m()");

        List<Overlap> overlaps = overlapsFinder.findOverlaps(target.build(), somePattern(pattern));

        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("P", "m()").withEdge("P", PARAMETER, "m()");
        assertThat(overlaps, contains(overlap));
    }

    /**
     * When the target has more alternatives than the pattern, the greedy strategy explores only one of them. To reduce
     * the number of false positives, we ensure that corresponding direct and indirect edges always get mapped together.
     */
    @Test
    public void mapsCorrespondingDirectAndIndirectEdges() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("src()", "sink()").withEdge("src()", PARAMETER, "sink()")
                .withDataNode("P1", "P").withEdge("P1", PARAMETER, "sink()")
                .withDataNode("P2", "P").withEdge("src()", DEFINITION, "P2").withEdge("P2", PARAMETER, "sink()");
        // From the four direct edges in this target, the two para edges have two alternative mappings each, while
        // the two def edges have one alternative mapping each, hence, they get mapped first. If the def edge to P3 is
        // mapped first, P2 -(para)-> sink() cannot be mapped anymore, hence, we find a false-positive violation. To
        // avoid this, we give priority to the corresponding direct para edge after mapping the indirect para edge.
        TestAUGBuilder target = buildAUG().withActionNodes("src()", "sink()").withEdge("src()", PARAMETER, "sink()")
                .withDataNode("P1", "P").withEdge("P1", PARAMETER, "sink()")
                .withDataNode("P3", "P").withEdge("src()", DEFINITION, "P3")
                .withDataNode("P2", "P").withEdge("src()", DEFINITION, "P2").withEdge("P2", PARAMETER, "sink()");

        List<Overlap> overlaps = overlapsFinder.findOverlaps(target.build(), somePattern(pattern));

        TestOverlapBuilder overlap = buildOverlap(pattern, target)
                .withNodes("src()", "sink()").withEdge("src()", PARAMETER, "sink()")
                .withNode("P1").withEdge("P1", PARAMETER, "sink()")
                .withNode("P2").withEdge("src()", DEFINITION, "P2").withEdge("P2", PARAMETER, "sink()");
        assertThat(overlaps, contains(overlap));
    }

    /**
     * If an action has multiple parameters (of the same type), they all have equally many mapping alternatives,
     * i.e., we would pick one of them at random as the next extension candidate. As long as the parameters have no
     * other connections, this is fine. However, if one parameter has other connections, we might be unlucky with
     * the selection, leaving some of these additional connections unnecessarily unmappend. To mitigate this problem,
     * we prefer parameter nodes with a higher edge degree.
     */
    @Test
    public void prefersParameterWithMultipleConnectionsToReduceRiskOfUnluckyMapping() {
        TestAUGBuilder pattern = buildAUG().withActionNode("sur()", "Utilities.isSurrogatePair()")
                .withActionNode("con()", "Utilities.convertToUtf32()")
                .withDataNode("int")
                .withEdge("int", PARAMETER, "sur()").withEdge("int", PARAMETER, "con()")
                .withEdge("sur()", SELECTION, "con()").withEdge("sur()", ORDER, "con()");
        TestAUGBuilder target = buildAUG().withActionNode("sur()", "Utilities.isSurrogatePair()")
                .withActionNode("con()", "Utilities.convertToUtf32()")
                .withDataNode("int")
                .withDataNode("i1", "int").withEdge("i1", PARAMETER, "sur()")
                .withDataNode("i2", "int").withEdge("i2", PARAMETER, "con()")
                .withEdge("int", PARAMETER, "sur()").withEdge("int", PARAMETER, "con()")
                .withEdge("sur()", SELECTION, "con()").withEdge("sur()", ORDER, "con()");

        List<Overlap> overlaps = overlapsFinder.findOverlaps(target.build(), somePattern(pattern));

        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("sur()", "con()", "int")
                .withEdge("int", PARAMETER, "sur()").withEdge("int", PARAMETER, "con()")
                .withEdge("sur()", SELECTION, "con()").withEdge("sur()", ORDER, "con()");
        assertThat(overlaps, contains(overlap));
    }
}
