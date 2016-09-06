package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import egroum.EGroumDataNode;
import egroum.EGroumNode;

import java.util.*;

public class InstanceFinder {
    private static class CommonNode {
        private EGroumNode targetNode;
        private EGroumNode patternNode;

        CommonNode(EGroumNode targetNode, EGroumNode patternNode) {
            this.targetNode = targetNode;
            this.patternNode = patternNode;
        }
    }

    public static List<Instance> findInstances(AUG target, AUG pattern) {
        Map<String, Set<EGroumNode>> patternNodesByLabel = getMeaningfulActionNodesByLabel(pattern);

        Set<CommonNode> initialNodes = new HashSet<>();
        for (EGroumNode targetNode : target.vertexSet()) {
            String label = targetNode.getLabel();
            if (patternNodesByLabel.containsKey(label)) {
                for (EGroumNode patternNode : patternNodesByLabel.get(label)) {
                    initialNodes.add(new CommonNode(targetNode, patternNode));
                }

            }
        }

        List<Instance> instances = new ArrayList<>();
        for (CommonNode node : initialNodes) {
            instances.add(findInstance(target, pattern, node));
        }

        return instances;
    }

    private static Instance findInstance(AUG target, AUG pattern, CommonNode node) {
        Instance instance = new Instance(pattern, new HashSet<>());
        instance.addVertex(node.patternNode);

        EGroumNode patternReceiver = pattern.getReceiver(node.patternNode);
        if (patternReceiver != null) {
            EGroumDataNode targetReceiver = target.getReceiver(node.targetNode);
            if (targetReceiver.getLabel().equals(patternReceiver.getLabel())) {
                instance.addVertex(patternReceiver);
                instance.addEdge(patternReceiver, node.patternNode);
            }
        }

        return instance;
    }

    private static Map<String, Set<EGroumNode>> getMeaningfulActionNodesByLabel(AUG aug) {
        Map<String, Set<EGroumNode>> nodesByLabel = new HashMap<>();
        for (EGroumNode node : aug.vertexSet()) {
            if (node.isMeaningfulAction()) {
                String label = node.getLabel();
                if (!nodesByLabel.containsKey(label)) {
                    nodesByLabel.put(label, new HashSet<>());
                }
                nodesByLabel.get(label).add(node);
            }
        }
        return nodesByLabel;
    }

}
