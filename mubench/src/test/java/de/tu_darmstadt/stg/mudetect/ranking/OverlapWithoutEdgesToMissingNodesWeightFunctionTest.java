package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.instance;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

public class OverlapWithoutEdgesToMissingNodesWeightFunctionTest {
    @Test
    public void noMissingElements() {
        Overlap instance = instance(buildAUG().withActionNode("A"));

        double weight = getWeight(instance, node -> 1);

        assertThat(weight, is(1.0));
    }

    @Test
    public void noMissingElements_withEdge() {
        Overlap instance = instance(buildAUG().withActionNodes("A", "B").withEdge("A", ORDER, "B"));

        double weight = getWeight(instance, node -> 1);

        assertThat(weight, is(1.0));
    }

    @Test
    public void excludesEdgeToMissingNode() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withEdge("A", ORDER, "B");
        TestAUGBuilder target = buildAUG().withActionNode("A");
        Overlap violation = buildOverlap(pattern, target).withNode("A").build();

        double weight = getWeight(violation, node -> 1);

        assertThat(weight, is(0.5));
    }

    @Test
    public void considersNodeImportance() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B").withEdge("A", ORDER, "B");
        Node nodeB = pattern.getNode("B");
        TestAUGBuilder target = buildAUG().withActionNode("A");
        Overlap violation = buildOverlap(pattern, target).withNode("A").build();

        double weightWithEqualImportance = getWeight(violation, node -> 1);
        double weightWithMissingNodeDoubleImportance = getWeight(violation, node -> node == nodeB ? 2 : 1);

        assertThat(weightWithEqualImportance, is(lessThan(weightWithMissingNodeDoubleImportance)));
    }

    @Test
    public void considerNodeImportance2() {
        TestAUGBuilder pattern = buildAUG().withActionNodes("A", "B", "C")
                .withEdge("A", ORDER, "B").withEdge("A", ORDER, "C").withEdge("B", ORDER, "C");
        Node nodeC = pattern.getNode("C");
        TestAUGBuilder target = buildAUG().withActionNodes("A", "B").withEdge("A", ORDER, "B");
        Overlap violation = buildOverlap(pattern, target).withNodes("A", "B").withEdge("A", ORDER, "B").build();

        double weightWithEqualImportance = getWeight(violation, node -> 1);
        double weightWithMissingNodeDoubleImportance = getWeight(violation, node -> node == nodeC ? 2 : 1);

        assertThat(weightWithEqualImportance, is(lessThan(weightWithMissingNodeDoubleImportance)));
    }

    private double getWeight(Overlap violation, NodeWeightFunction a) {
        return new OverlapWithoutEdgesToMissingNodesWeightFunction(a).getWeight(violation, null, null);
    }
}
