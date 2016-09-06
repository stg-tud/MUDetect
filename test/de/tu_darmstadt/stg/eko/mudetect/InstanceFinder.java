package de.tu_darmstadt.stg.eko.mudetect;

import de.tu_darmstadt.stg.eko.mudetect.model.AUG;
import egroum.EGroumNode;

import java.util.*;
import java.util.function.Function;

public class InstanceFinder {
    public static List<Instance> findInstances(AUG target, AUG pattern) {
        Map<String, Set<EGroumNode>> patternNodesByLabel = getNodelByLabel(pattern);

        Set<EGroumNode> occurringPatternNodes = new HashSet<>();
        for (EGroumNode node : target.vertexSet()) {
            String label = node.getLabel();
            if (patternNodesByLabel.containsKey(label)) {
                occurringPatternNodes.addAll(patternNodesByLabel.get(label));
            }
        }

        if (occurringPatternNodes.isEmpty()) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(new Instance(pattern, occurringPatternNodes));
        }
    }

    private static Map<String, Set<EGroumNode>> getNodelByLabel(AUG aug) {
        Map<String, Set<EGroumNode>> nodesByLabel = new HashMap<>();
        for (EGroumNode node : aug.vertexSet()) {
            String label = node.getLabel();
            if (!nodesByLabel.containsKey(label)) {
                nodesByLabel.put(label, new HashSet<>());
            }
            nodesByLabel.get(label).add(node);
        }
        return nodesByLabel;
    }
}
