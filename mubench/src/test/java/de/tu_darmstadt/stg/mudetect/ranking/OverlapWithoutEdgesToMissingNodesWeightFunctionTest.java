package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.instance;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

public class OverlapWithoutEdgesToMissingNodesWeightFunctionTest {
    @Test
    public void noMissingElements() throws Exception {
        Overlap instance = instance(buildAUG().withActionNode("A"));

        double weight = getWeight(instance, node -> 1);

        assertThat(weight, is(1.0));
    }

    @Test
    public void noMissingElements_withEdge() throws Exception {
        Overlap instance = instance(buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B"));

        double weight = getWeight(instance, node -> 1);

        assertThat(weight, is(1.0));
    }

    @Test
    public void excludesEdgeToMissingNode() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNode("A");
        Overlap violation = buildOverlap(target, pattern).withNode("A").build();

        double weight = getWeight(violation, node -> 1);

        assertThat(weight, is(0.5));
    }

    @Test
    public void considersNodeImportance() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNode("A");
        Overlap violation = buildOverlap(target, pattern).withNode("A").build();

        double weightWithEqualImportance = getWeight(violation, node -> 1);
        double weightWithMissingNodeDoubleImportance = getWeight(violation, node -> node.getLabel().equals("B") ? 2 : 1);

        assertThat(weightWithEqualImportance, is(lessThan(weightWithMissingNodeDoubleImportance)));
    }

    @Test
    public void considerNodeImportance2() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B", "C")
                .withDataEdge("A", ORDER, "B").withDataEdge("A", ORDER, "C").withDataEdge("B", ORDER, "C");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        Overlap violation = buildOverlap(target, pattern).withNodes("A", "B").withEdge("A", ORDER, "B").build();

        double weightWithEqualImportance = getWeight(violation, node -> 1);
        double weightWithMissingNodeDoubleImportance = getWeight(violation, node -> node.getLabel().equals("C") ? 2 : 1);

        assertThat(weightWithEqualImportance, is(lessThan(weightWithMissingNodeDoubleImportance)));
    }

    private double getWeight(Overlap violation, NodeWeightFunction a) {
        return new OverlapWithoutEdgesToMissingNodesWeightFunction(a).getWeight(violation, null, null);
    }
}
