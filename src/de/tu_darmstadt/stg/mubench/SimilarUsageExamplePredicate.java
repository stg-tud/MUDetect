package de.tu_darmstadt.stg.mubench;

import egroum.EGroumGraph;

import egroum.EGroumNode;
import mining.TypeUsageExamplePredicate;

import de.tu_darmstadt.stg.mudetect.model.AUG;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class SimilarUsageExamplePredicate extends TypeUsageExamplePredicate {
    private final Set<String> labels;

    public static SimilarUsageExamplePredicate examplesSimilarTo(AUG misuseInstance, API api) {
        Set<String> labels = misuseInstance.vertexSet().stream().map(EGroumNode::getLabel).collect(Collectors.toSet());
        return new SimilarUsageExamplePredicate(labels, api);
    }

    private SimilarUsageExamplePredicate(Set<String> labels, API api) {
        super(api.getName());
        this.labels = labels;
    }

    @Override
	public boolean matches(EGroumGraph graph) {
        return labels.isEmpty() || !Collections.disjoint(graph.getNodeLabels(), labels);
    }
}
