package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mudetect.VeryUnspecificReceiverTypePredicate;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import edu.iastate.cs.mudetect.mining.Configuration;

import java.util.HashSet;

import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.REPETITION;
import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.SELECTION;

class DefaultOverlapFinderConfig extends AlternativeMappingsOverlapsFinder.Config {
    DefaultOverlapFinderConfig(Configuration config) {
        // SMELL this is a hack, we should introduce a edgeLabel function instead, to handle this equally in mining and detection
        HashSet<Object> equivalentEdgeLabels = new HashSet<>();
        equivalentEdgeLabels.add(SELECTION.getLabel());
        equivalentEdgeLabels.add(REPETITION.getLabel());

        isStartNode = super.isStartNode.and(new VeryUnspecificReceiverTypePredicate().negate());
        nodeMatcher = new EquallyLabelledNodeMatcher(config.nodeToLabel);
        edgeOrder = new DataEdgeTypePriorityOrder();
        edgeMatcher = super.edgeMatcher.or((el1, el2) -> equivalentEdgeLabels.contains(el1) && equivalentEdgeLabels.contains(el2));
    }
}
