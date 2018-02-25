package de.tu_darmstadt.stg.mudetect.overlapsfinder;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.REPETITION;
import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.SELECTION;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.overlapsfinder.OverlapsFinderTestUtils.assertFindsOverlaps;
import static de.tu_darmstadt.stg.mudetect.overlapsfinder.OverlapsFinderTestUtils.findOverlaps;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static de.tu_darmstadt.stg.mudetect.utils.CollectionUtils.only;

public class FindViolationsTest {
    @Test
    public void findsMissingNode() {
        TestAUGBuilder target = buildAUG().withActionNode("C.m()");
        TestAUGBuilder pattern = buildAUG().withActionNode("C.m()")
                .withActionNode("C.n()").withEdge("C.m()", ORDER, "C.n()");

        TestOverlapBuilder violation = buildOverlap(pattern, target).withNode("C.m()");
        assertFindsOverlaps(pattern, target, violation);
    }

    @Test
    public void excludesNonEqualNode() {
        TestAUGBuilder pattern = buildAUG().withActionNode("A").withActionNode("B").withEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNode("A").withActionNode("C").withEdge("A", ORDER, "C");

        TestOverlapBuilder violation = buildOverlap(pattern, target).withNode("A");
        assertFindsOverlaps(pattern, target, violation);
    }

    @Test
    public void ignoresNonEqualEdge() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withEdge("A", PARAMETER, "B");

        TestOverlapBuilder violation1 = buildOverlap(pattern, target).withNode("A");
        TestOverlapBuilder violation2 = buildOverlap(pattern, target).withNode("B");
        assertFindsOverlaps(pattern, target, violation1, violation2);
    }

    @Test
    public void ignoresReverseEdge() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withEdge("B", ORDER, "A");

        TestOverlapBuilder violation1 = buildOverlap(pattern, target).withNode("A");
        TestOverlapBuilder violation2 = buildOverlap(pattern, target).withNode("B");
        assertFindsOverlaps(pattern, target, violation1, violation2);
    }

    @Test
    public void mapsTargetEdgeOnlyOnce() {
        TestAUGBuilder pattern = buildAUG().withActionNode("A").withActionNode("B1", "B").withActionNode("B2", "B")
                .withEdge("A", SELECTION, "B1").withEdge("A", SELECTION, "B2");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withEdge("A", SELECTION, "B");

        List<Overlap> overlaps = findOverlaps(pattern, target);

        assertThat(only(overlaps).getNodeSize(), is(2));
    }

    @Test
    public void mapsPatternNodeOnlyOnce() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B")
                .withEdge("A", PARAMETER, "B")
                .withEdge("A", SELECTION, "B");

        TestAUGBuilder target = buildAUG().withActionNode("A1", "A").withActionNode("A2", "A").withActionNode("B")
                .withEdge("A1", PARAMETER, "B")
                .withEdge("A2", SELECTION, "B");

        TestOverlapBuilder violation1 = buildOverlap(pattern, target).withNode("A1", "A").withNode("B")
                .withEdge("A1", "A", PARAMETER, "B", "B");
        TestOverlapBuilder violation2 = buildOverlap(pattern, target).withNode("A2", "A").withNode("B")
                .withEdge("A2", "A", SELECTION, "B", "B");

        assertFindsOverlaps(pattern, target, violation1, violation2);
    }

    @Test
    public void mapsTargetNodeOnlyOnce() {
        TestAUGBuilder pattern = buildAUG().withActionNode("A1", "A").withActionNode("A2", "A").withActionNode("B")
                .withEdge("A1", PARAMETER, "B")
                .withEdge("A2", SELECTION, "B");

        TestAUGBuilder target = buildAUG().withActionNodes("A", "B")
                .withEdge("A", PARAMETER, "B")
                .withEdge("A", SELECTION, "B");

        List<Overlap> overlaps = findOverlaps(pattern, target);

        assertThat(only(overlaps).getNodeSize(), is(2));
    }

    @Test
    public void findsInstanceAndPartialInstance() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B", "C")
                .withEdge("A", SELECTION, "B").withEdge("B", SELECTION, "C");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "C").withActionNode("B1", "B").withActionNode("B2", "B")
                .withEdge("A", SELECTION, "B1").withEdge("A", SELECTION, "B2").withEdge("B1", SELECTION, "C");

        TestOverlapBuilder instance = buildOverlap(pattern, target).withNodes("A", "C").withNode("B1", "B")
                .withEdge("A", "A", SELECTION, "B1", "B").withEdge("B1", "B", SELECTION, "C", "C");
        TestOverlapBuilder violation = buildOverlap(pattern, target).withNode("A").withNode("B2", "B")
                .withEdge("A", "A", SELECTION, "B2", "B");

        assertFindsOverlaps(pattern, target, instance, violation);
    }

    /**
     * Issue: The pattern expects three calls <code>a(); b(); a();</code>, but the target is
     * <code>a(); a(); b();</code>. There's one overlap that matches all calls, but misses the edge from b() to a() and
     * a second overlap that matches the <code>a(); b();</code> suffix from the target to the prefix of the pattern. We
     * currently report only the larger overlap, since it covers all nodes covered by the smaller one and better
     * explains the actual problem (the reversed call order).
     */
    @Test
    public void findsBothPartialOverlaps() {
        TestAUGBuilder pattern = buildAUG().withActionNode("a1", "a").withActionNode("a2", "a").withActionNode("b")
                .withEdge("a1", SELECTION, "a2").withEdge("a1", SELECTION, "b").withEdge("b", SELECTION, "a2");
        TestAUGBuilder target = buildAUG().withActionNode("a1", "a").withActionNode("a2", "a").withActionNode("b")
                .withEdge("a1", SELECTION, "a2").withEdge("a1", SELECTION, "b").withEdge("a2", SELECTION, "b");

        TestOverlapBuilder overlap = buildOverlap(pattern, target).withNodes("a1", "a2", "b")
                .withEdge("a1", SELECTION, "a2").withEdge("a1", SELECTION, "b");

        assertFindsOverlaps(pattern, target, overlap);
    }

    /**
     * Issue: The pattern expects three calls <code>a(); a(); b();</code> where the two last calls may occur in
     * arbitrary order, i.e., there's no edge between them. In the target, the calls occur in a fixed order, i.e., there
     * is an edge between them, which happens to correspond to an edge in the pattern. We don't want
     * <code>a(); b();</code> this to be reported as a partial instance, since it's really not.
     */
    @Test
    public void filtersAdditionalOrderEdgeBetweenNodesFromAnInstance() {
        TestAUGBuilder pattern = buildAUG().withActionNode("a1", "a").withActionNode("a2", "a").withActionNode("b")
                .withEdge("a1", SELECTION, "a2").withEdge("a1", SELECTION, "b");
        TestAUGBuilder target = buildAUG().withActionNode("a1", "a").withActionNode("a2", "a").withActionNode("b")
                .withEdge("a1", SELECTION, "a2").withEdge("a1", SELECTION, "b").withEdge("a2", SELECTION, "b");

        TestOverlapBuilder instance = buildOverlap(pattern, target)
                .withNodes("a1", "a2").withEdge("a1", SELECTION, "a2")
                .withNode("b").withEdge("a1", SELECTION, "b");

        assertFindsOverlaps(pattern, target, instance);
    }

    /**
     * Issue: When expanding the pattern, the finder may pick an edge that is not mappable in the target and then
     * continue extending from that edge's target. Since the edge is not mappable, no edges from its target are and,
     * thus, the result does not contain correspondents to these edges, even if there is another path in the
     * target graph that leads to them. To solve this problem, we make the algorithm prioritize mappable edges.
     */
    @Test
    public void prioritizesMappableEdges() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A") // the algorithm will start extending from this node
                .withDataNodes("B", "C", "D", "E", "F")
                .withEdge("B", SELECTION, "A")
                .withEdge("C", SELECTION, "A")
                .withEdge("D", SELECTION, "A")
                .withEdge("E", SELECTION, "A")
                .withEdge("F", REPETITION, "A") // this is the edge that makes the connection
                .withEdge("F", REPETITION, "B")
                .withEdge("F", REPETITION, "C")
                .withEdge("F", REPETITION, "D")
                .withEdge("F", REPETITION, "E");

        TestAUGBuilder target = buildAUG().withActionNodes("A") // the algorithm will start extending from this node
                .withDataNodes("B", "C", "D", "E", "F")
                .withEdge("F", REPETITION, "A") // this is the edge that makes the connection
                .withEdge("F", REPETITION, "B")
                .withEdge("F", REPETITION, "C")
                .withEdge("F", REPETITION, "D")
                .withEdge("F", REPETITION, "E");

        TestOverlapBuilder violation = buildOverlap(pattern, target).withNodes("A", "B", "C", "D", "E", "F")
                .withEdge("F", REPETITION, "A")
                .withEdge("F", REPETITION, "B")
                .withEdge("F", REPETITION, "C")
                .withEdge("F", REPETITION, "D")
                .withEdge("F", REPETITION, "E");

        assertFindsOverlaps(pattern, target, violation);
    }
}
