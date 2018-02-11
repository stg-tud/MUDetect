package de.tu_darmstadt.stg.mudetect.overlapsfinder;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import org.junit.Before;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.SELECTION;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.instance;
import static de.tu_darmstadt.stg.mudetect.overlapsfinder.OverlapsFinderTestUtils.findsOverlaps;
import static org.junit.Assert.assertThat;

public class ConditionMappingTest {
    private AlternativeMappingsOverlapsFinder overlapsFinder;

    @Before
    public void setUp() throws Exception {
        overlapsFinder = new AlternativeMappingsOverlapsFinder(
                new AlternativeMappingsOverlapsFinder.Config() {{
                    nodeMatcher = new EquallyLabelledNodeMatcher();
                    matchEntireConditions = true;
                }});
    }

    @Test
    public void mapsEqualConditions() throws Exception {
        TestAUGBuilder target = buildAUG().withActionNodes("A", "<").withDataNodes("int", "long")
                .withCondEdge("<", SELECTION, "A").withEdge("int", PARAMETER, "<").withEdge("long", PARAMETER, "<");
        Overlap instance = instance(target);

        assertThat(overlapsFinder, findsOverlaps(target, target, instance));
    }

    @Test
    public void skipsEntireConditionOnDifferentArguments() throws Exception {
        TestAUGBuilder target = buildAUG().withActionNodes("A", "<").withDataNodes("int", "float")
                .withCondEdge("<", SELECTION, "A").withEdge("int", PARAMETER, "<").withEdge("float", PARAMETER, "<");
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "<").withDataNodes("int", "long")
                .withCondEdge("<", SELECTION, "A").withEdge("int", PARAMETER, "<").withEdge("long", PARAMETER, "<");
        Overlap overlap = buildOverlap(target, pattern).withNode("A").build();

        assertThat(overlapsFinder, findsOverlaps(target, pattern, overlap));
    }
}
