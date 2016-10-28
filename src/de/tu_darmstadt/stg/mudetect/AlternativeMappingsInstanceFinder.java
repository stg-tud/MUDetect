package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.*;
import java.util.stream.Collectors;

public class AlternativeMappingsInstanceFinder implements InstanceFinder {

    private static class Extension {
        private final AUG target;
        private final Pattern pattern;
        private final EGroumNode firstPatternNode;
        private final Set<InstanceBuilder> alternatives = new HashSet<>();

        private final Queue<EGroumEdge> patternExtensionEdges = new LinkedList<>();

        private Extension(AUG target, Pattern pattern, EGroumNode firstPatternNode) {
            this.target = target;
            this.pattern = pattern;
            this.firstPatternNode = firstPatternNode;

            patternExtensionEdges.addAll(pattern.edgesOf(firstPatternNode));
        }

        protected void addFirstPatternNodeAlternative(EGroumNode targetNodeAlternative) {
            InstanceBuilder alternative = new InstanceBuilder(target, pattern);
            alternative.map(targetNodeAlternative, firstPatternNode);
            alternatives.add(alternative);
        }

        boolean hasNextPatternExtensionEdge() {
            return !patternExtensionEdges.isEmpty();
        }

        EGroumEdge nextPatternExtensionEdge() {
            return patternExtensionEdges.poll();
        }

        public boolean hasAlternatives() {
            return getNumberOfAlternatives() > 1;
        }

        public int getNumberOfAlternatives() {
            return alternatives.size();
        }

        public Collection<Instance> getInstances() {
            return alternatives.stream().map(InstanceBuilder::build).collect(Collectors.toSet());
        }
    }

    @Override
    public List<Instance> findInstances(AUG target, Pattern pattern) {
        List<Instance> instances = new ArrayList<>();

        Collection<Extension> singleNodeExtensions = getSingleNodeExtensions(target, pattern);
        for (Extension extension : singleNodeExtensions) {
            extend(extension, extension.firstPatternNode);
            instances.addAll(extension.getInstances());
        }

        removeSubInstances(instances);
        return instances;
    }


    private Collection<Extension> getSingleNodeExtensions(AUG target, Pattern pattern) {
        Map<String, Set<EGroumNode>> patternNodesByLabel = pattern.getMeaningfulActionNodesByLabel();

        Map</* pattern node */ EGroumNode, Extension> alternatives = new HashMap<>();
        for (EGroumNode targetNode : target.vertexSet()) {
            String label = targetNode.getLabel();
            if (patternNodesByLabel.containsKey(label)) {
                for (EGroumNode patternNode : patternNodesByLabel.get(label)) {
                    if (!alternatives.containsKey(patternNode)) {
                        alternatives.put(patternNode, new Extension(target, pattern, patternNode));
                    }
                    alternatives.get(patternNode).addFirstPatternNodeAlternative(targetNode);
                }
            }
        }
        return alternatives.values();
    }

    private void extend(Extension extension, EGroumNode currentPatternNode) {
        if (extension.hasNextPatternExtensionEdge()) {
            EGroumEdge patternEdge = extension.nextPatternExtensionEdge();

            for (InstanceBuilder alternative : extension.alternatives) {
                // find alternative target edges to map to
                EGroumNode mappedTargetNode = alternative.getMappedTargetNode(currentPatternNode);
                Set<EGroumEdge> targetEdges = extension.target.edgesOf(mappedTargetNode);
                if (!targetEdges.isEmpty()) {
                    EGroumEdge targetEdge = targetEdges.iterator().next();

                    alternative.map(targetEdge.getTarget(), patternEdge.getTarget());
                    alternative.map(targetEdge, patternEdge);
                }
            }
        }
    }

    private void removeSubInstances(List<Instance> instances) {
        for (int i = 0; i < instances.size(); i++) {
            Instance instance1 = instances.get(i);
            for (int j = i + 1; j < instances.size(); j++) {
                Instance instance2 = instances.get(j);
                if (instance2.isSubInstanceOf(instance1)) {
                    instances.remove(j);
                    j--;
                } else if (instance1.isSubInstanceOf(instance2)) {
                    instances.remove(i);
                    i--;
                    break;
                }
            }
        }
    }
}
