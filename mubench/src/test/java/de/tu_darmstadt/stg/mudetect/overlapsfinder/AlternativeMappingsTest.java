package de.tu_darmstadt.stg.mudetect.overlapsfinder;

import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.DEFINITION;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.overlapsfinder.OverlapsFinderTestUtils.*;

public class AlternativeMappingsTest {
    /**
     * We frequently observe that targets have multiple alternatives for data nodes incoming or outgoing of action
     * nodes, while the pattern has fewer. Since we cannot explore all possible mappings, due to a possible state
     * explosion, we use the number of available alternatives to guide to exploration, i.e., the extension prefers
     * edges that have the fewest alternatives mappings (considering both pattern and target). In the test scenario
     * this means we first extend by <code>getB() -PAR-> A</code> and then by <code>getB() -DEF-> B</code>, which leaves
     * only one valid alternative for <code>B -PAR-> A</code>.
     */
    @Test
    public void findsMaximalAlternativeMappingToTarget() throws Exception {
        TestAUGBuilder target = buildAUG().withActionNodes("A", "getB()")
                .withDataNode("B1", "B").withDataEdge("B1", PARAMETER, "A")
                .withDataNode("B2", "B").withDataEdge("B2", PARAMETER, "A")
                .withDataNode("B3", "B").withDataEdge("B3", PARAMETER, "A")
                .withDataNode("B4", "B").withDataEdge("B4", PARAMETER, "A")
                .withDataNode("B5", "B").withDataEdge("B5", PARAMETER, "A")
                .withDataEdge("getB()", DEFINITION, "B3").withDataEdge("getB()", PARAMETER, "A");
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "getB()")
                .withDataNode("B").withDataEdge("B", PARAMETER, "A")
                .withDataEdge("getB()", DEFINITION, "B").withDataEdge("getB()", PARAMETER, "A");

        TestOverlapBuilder instance = buildOverlap(target, pattern).withNodes("A", "getB()")
                .withNode("B3", "B").withEdge("B3", "B", PARAMETER, "A", "A")
                .withEdge("getB()", "getB()", DEFINITION, "B3", "B").withEdge("getB()", PARAMETER, "A");

        assertFindsOverlaps(pattern, target, instance);
    }
}
