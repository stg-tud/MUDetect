package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mudetect.VeryUnspecificReceiverTypePredicate;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledEdgeMatcher;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.matcher.SelAndRepSameLabelProvider;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import edu.iastate.cs.mudetect.mining.Configuration;

import java.util.HashSet;

import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.REPETITION;
import static de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge.ConditionType.SELECTION;

class DefaultOverlapFinderConfig extends AlternativeMappingsOverlapsFinder.Config {
    DefaultOverlapFinderConfig(Configuration config) {
        isStartNode = super.isStartNode.and(new VeryUnspecificReceiverTypePredicate().negate());
        nodeMatcher = new EquallyLabelledNodeMatcher(config.labelProvider);
        edgeMatcher = new EquallyLabelledEdgeMatcher(config.labelProvider);
        edgeOrder = new DataEdgeTypePriorityOrder();
    }
}
