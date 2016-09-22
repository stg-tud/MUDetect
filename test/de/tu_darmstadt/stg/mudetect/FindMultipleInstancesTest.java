package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.AUGBuilder;
import egroum.EGroumDataEdge;
import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.junit.Test;

import java.util.*;

import static de.tu_darmstadt.stg.mudetect.FindMultipleInstancesTest.InstanceBuilder.createInstance;
import static de.tu_darmstadt.stg.mudetect.model.AUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.AUGBuilder.extend;
import static de.tu_darmstadt.stg.mudetect.model.InstanceTestUtils.hasInstance;
import static egroum.EGroumDataEdge.Type.ORDER;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FindMultipleInstancesTest {
    @Test
    public void mapsAnEdgeOnlyOnce() throws Exception {
        AUGBuilder builder = buildAUG().withActionNodes("A", "B").withDataEdge("A", ORDER, "B");
        AUGBuilder targetBuilder = extend(builder).withActionNode("B2", "B").withDataEdge("A", ORDER, "B2");

        Instance instance1 = createInstance(targetBuilder, builder)
                .withNode("A", "A")
                .withNode("B", "B")
                .withEdge("A", "A", ORDER, "B", "B").build();
        Instance instance2 = createInstance(targetBuilder, builder)
                .withNode("A", "A")
                .withNode("B2", "B")
                .withEdge("A", "A", ORDER, "B2", "B").build();

        AUG pattern = builder.build();
        AUG target = targetBuilder.build();

        List<Instance> instances = new GreedyInstanceFinder().findInstances(target, pattern);

        assertThat(instances, hasSize(2));
        assertThat(instances, hasItems(instance1, instance2));
    }

    static class InstanceBuilder {
        private AUGBuilder targetAUGBuilder;
        private AUGBuilder patternAUGBuilder;

        public static InstanceBuilder createInstance(AUGBuilder targetAUGBuilder, AUGBuilder patternAUGBuilder) {
            return new InstanceBuilder(targetAUGBuilder, patternAUGBuilder);
        }

        private final Map<EGroumNode, EGroumNode> targetNodeByPatternNode = new HashMap<>();
        private final Map<EGroumEdge, EGroumEdge> targetEdgeByPatternEdge = new HashMap<>();

        private InstanceBuilder(AUGBuilder targetAUGBuilder, AUGBuilder patternAUGBuilder) {
            this.targetAUGBuilder = targetAUGBuilder;
            this.patternAUGBuilder = patternAUGBuilder;
        }

        public InstanceBuilder withNode(String targetNodeId, String patternNodeId) {
            EGroumNode targetNode = targetAUGBuilder.getNode(targetNodeId);
            if (targetNodeByPatternNode.containsValue(targetNode)) {
                throw new IllegalArgumentException("Target node '" + targetNodeId + "' is already mapped.");
            }
            EGroumNode patternNode = patternAUGBuilder.getNode(patternNodeId);
            if (targetNodeByPatternNode.containsKey(patternNode)) {
                throw new IllegalArgumentException("Pattern node '" + patternNodeId + "' is already mapped.");
            }
            targetNodeByPatternNode.put(patternNode, targetNode);
            return this;
        }

        public InstanceBuilder withEdge(String targetSourceNodeId, String patternSourceNodeId, EGroumDataEdge.Type type, String targetTargetNodeId, String patternTargetNodeId) {
            targetEdgeByPatternEdge.put(
                    patternAUGBuilder.getEdge(patternSourceNodeId, type, patternTargetNodeId),
                    targetAUGBuilder.getEdge(targetSourceNodeId, type, targetTargetNodeId));
            return this;
        }

        public Instance build() {
            return new Instance(targetAUGBuilder.build(), patternAUGBuilder.build(), targetNodeByPatternNode, targetEdgeByPatternEdge);
        }
    }
}
