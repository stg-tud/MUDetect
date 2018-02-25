package de.tu_darmstadt.stg.mudetect.overlapsfinder;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledEdgeMatcher;
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
    public void setUp() {
        overlapsFinder = new AlternativeMappingsOverlapsFinder(
                new AlternativeMappingsOverlapsFinder.Config() {{
                    BaseAUGLabelProvider labelProvider = new BaseAUGLabelProvider();
                    nodeMatcher = new EquallyLabelledNodeMatcher(labelProvider);
                    edgeMatcher = new EquallyLabelledEdgeMatcher(labelProvider);
                    matchEntireConditions = true;
                }});
    }

    @Test
    public void mapsEqualConditions() {
        TestAUGBuilder target = buildAUG().withActionNodes("A", "<").withDataNodes("int", "long")
                .withEdge("<", SELECTION, "A").withEdge("int", PARAMETER, "<").withEdge("long", PARAMETER, "<");
        Overlap instance = instance(target);

        assertThat(overlapsFinder, findsOverlaps(target, target, instance));
    }

    @Test
    public void skipsEntireConditionOnDifferentArguments() {
        TestAUGBuilder target = buildAUG().withActionNodes("A", "<").withDataNodes("int", "float")
                .withEdge("<", SELECTION, "A").withEdge("int", PARAMETER, "<").withEdge("float", PARAMETER, "<");
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "<").withDataNodes("int", "long")
                .withEdge("<", SELECTION, "A").withEdge("int", PARAMETER, "<").withEdge("long", PARAMETER, "<");
        Overlap overlap = buildOverlap(pattern, target).withNode("A").build();

        assertThat(overlapsFinder, findsOverlaps(target, pattern, overlap));
    }
}
