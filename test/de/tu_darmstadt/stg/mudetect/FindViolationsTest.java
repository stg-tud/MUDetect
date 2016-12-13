package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.OverlapsFinderTestUtils.assertFindsOverlaps;
import static de.tu_darmstadt.stg.mudetect.OverlapsFinderTestUtils.findOverlaps;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static egroum.EGroumDataEdge.Type.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static utils.CollectionUtils.only;

public class FindViolationsTest {
    @Test
    public void findsMissingNode() throws Exception {
        TestAUGBuilder target = buildAUG().withActionNode("C.m()");
        TestAUGBuilder pattern = buildAUG().withActionNode("C.m()")
                .withActionNode("C.n()").withDataEdge("C.m()", ORDER, "C.n()");

        TestOverlapBuilder violation = buildOverlap(target, pattern).withNode("C.m()");
        assertFindsOverlaps(pattern, target, violation);
    }

    @Test
    public void excludesNonEqualNode() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNode("A").withActionNode("B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNode("A").withActionNode("C").withDataEdge("A", ORDER, "C");

        TestOverlapBuilder violation = buildOverlap(target, pattern).withNode("A");
        assertFindsOverlaps(pattern, target, violation);
    }

    @Test
    public void ignoresNonEqualEdge() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withDataEdge("A", PARAMETER, "B");

        TestOverlapBuilder violation1 = buildOverlap(target, pattern).withNode("A");
        TestOverlapBuilder violation2 = buildOverlap(target, pattern).withNode("B");
        assertFindsOverlaps(pattern, target, violation1, violation2);
    }

    @Test
    public void ignoresReverseEdge() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withDataEdge("B", ORDER, "A");

        TestOverlapBuilder violation1 = buildOverlap(target, pattern).withNode("A");
        TestOverlapBuilder violation2 = buildOverlap(target, pattern).withNode("B");
        assertFindsOverlaps(pattern, target, violation1, violation2);
    }

    @Test
    public void mapsTargetEdgeOnlyOnce() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNode("A").withActionNode("B1", "B").withActionNode("B2", "B")
                .withDataEdge("A", ORDER, "B1").withDataEdge("A", ORDER, "B2");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");

        List<Overlap> overlaps = findOverlaps(pattern, target);

        assertThat(only(overlaps).getNodeSize(), is(2));
    }

    @Test
    public void mapsPatternNodeOnlyOnce() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B")
                .withDataEdge("A", PARAMETER, "B")
                .withCondEdge("A", "sel", "B");

        TestAUGBuilder target = buildAUG().withActionNode("A1", "A").withActionNode("A2", "A").withActionNode("B")
                .withDataEdge("A1", PARAMETER, "B")
                .withCondEdge("A2", "sel", "B");

        TestOverlapBuilder violation1 = buildOverlap(target, pattern).withNode("A1", "A").withNode("B")
                .withEdge("A1", "A", PARAMETER, "B", "B");
        TestOverlapBuilder violation2 = buildOverlap(target, pattern).withNode("A2", "A").withNode("B")
                .withEdge("A2", "A", CONDITION, "B", "B");

        assertFindsOverlaps(pattern, target, violation1, violation2);
    }

    @Test
    public void mapsTargetNodeOnlyOnce() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNode("A1", "A").withActionNode("A2", "A").withActionNode("B")
                .withDataEdge("A1", PARAMETER, "B")
                .withCondEdge("A2", "sel", "B");

        TestAUGBuilder target = buildAUG().withActionNodes("A", "B")
                .withDataEdge("A", PARAMETER, "B")
                .withCondEdge("A", "sel", "B");

        List<Overlap> overlaps = findOverlaps(pattern, target);

        assertThat(only(overlaps).getNodeSize(), is(2));
    }

    @Test
    public void findsInstanceAndPartialInstance() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B", "C")
                .withDataEdge("A", ORDER, "B").withDataEdge("B", ORDER, "C");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "C").withActionNode("B1", "B").withActionNode("B2", "B")
                .withDataEdge("A", ORDER, "B1").withDataEdge("A", ORDER, "B2").withDataEdge("B1", ORDER, "C");

        TestOverlapBuilder instance = buildOverlap(target, pattern).withNodes("A", "C").withNode("B1", "B")
                .withEdge("A", "A", ORDER, "B1", "B").withEdge("B1", "B", ORDER, "C", "C");
        TestOverlapBuilder violation = buildOverlap(target, pattern).withNode("A").withNode("B2", "B")
                .withEdge("A", "A", ORDER, "B2", "B");

        assertFindsOverlaps(pattern, target, instance, violation);
    }

    @Test @Ignore("We currently find two. I scheduled a discussion about a general rule to filter the 'false' one.")
    public void findsOnlyOneInstance() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNode("a1", "a").withActionNode("a2", "a").withActionNode("b")
                .withDataEdge("a1", ORDER, "a2").withDataEdge("a1", ORDER, "b").withDataEdge("b", ORDER, "a2");
        TestAUGBuilder target = buildAUG().withActionNode("a1", "a").withActionNode("a2", "a").withActionNode("b")
                .withDataEdge("a1", ORDER, "a2").withDataEdge("a1", ORDER, "b").withDataEdge("a2", ORDER, "b");

        List<Overlap> overlaps = findOverlaps(pattern, target);

        assertThat(overlaps, hasSize(1));
    }

    /**
     * Issue: What we have here is a pattern instance with an additional edge that matches an edge from the pattern. We
     * expect the detection to find the instance. I'm undecided whether we want it to find a violation consisting only
     * of the additional edge with its source and target nodes, since I'm yet to see a real scenario where this problem
     * occurs...
     */
    @Test
    public void considersEdgeDirectionBetweenEqualNodes() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNode("a1", "a").withActionNode("a2", "a").withActionNode("b")
                .withDataEdge("a1", ORDER, "a2").withDataEdge("a1", ORDER, "b");
        TestAUGBuilder target = buildAUG().withActionNode("a1", "a").withActionNode("a2", "a").withActionNode("b")
                .withDataEdge("a1", ORDER, "a2").withDataEdge("a1", ORDER, "b").withDataEdge("a2", ORDER, "b");

        TestOverlapBuilder instance = buildOverlap(target, pattern)
                .withNodes("a1", "a2").withEdge("a1", ORDER, "a2")
                .withNode("b").withEdge("a1", ORDER, "b");

        assertFindsOverlaps(pattern, target, instance);
    }

    /**
     * Issue: When expanding the pattern, the finder may pick an edge that is not mappable in the target and then
     * continue extending from that edge's target. Since the edge is not mappable, no edges from its target are and,
     * thus, the result does not contain correspondents to these edges, even if there is another path in the
     * target graph that leads to them. To solve this problem, we make the algorithm prioritize mappable edges.
     */
    @Test
    public void prioritizesMappableEdges() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A") // the algorithm will start extending from this node
                .withDataNodes("B", "C", "D", "E", "F")
                .withCondEdge("B", "sel", "A")
                .withCondEdge("C", "sel", "A")
                .withCondEdge("D", "sel", "A")
                .withCondEdge("E", "sel", "A")
                .withDataEdge("F", ORDER, "A") // this is the edge that makes the connection
                .withDataEdge("F", ORDER, "B")
                .withDataEdge("F", ORDER, "C")
                .withDataEdge("F", ORDER, "D")
                .withDataEdge("F", ORDER, "E");

        TestAUGBuilder target = buildAUG().withActionNodes("A") // the algorithm will start extending from this node
                .withDataNodes("B", "C", "D", "E", "F")
                .withDataEdge("F", ORDER, "A") // this is the edge that makes the connection
                .withDataEdge("F", ORDER, "B")
                .withDataEdge("F", ORDER, "C")
                .withDataEdge("F", ORDER, "D")
                .withDataEdge("F", ORDER, "E");

        TestOverlapBuilder violation = buildOverlap(target, pattern).withNodes("A", "B", "C", "D", "E", "F")
                .withEdge("F", ORDER, "A")
                .withEdge("F", ORDER, "B")
                .withEdge("F", ORDER, "C")
                .withEdge("F", ORDER, "D")
                .withEdge("F", ORDER, "E");

        assertFindsOverlaps(pattern, target, violation);
    }
}
