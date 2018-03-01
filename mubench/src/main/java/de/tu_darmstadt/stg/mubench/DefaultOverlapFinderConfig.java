package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mudetect.VeryUnspecificReceiverTypePredicate;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledEdgeMatcher;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.matcher.SelAndRepSameLabelProvider;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import edu.iastate.cs.mudetect.mining.Configuration;

class DefaultOverlapFinderConfig extends AlternativeMappingsOverlapsFinder.Config {
    DefaultOverlapFinderConfig(Configuration config) {
        isStartNode = super.isStartNode.and(new VeryUnspecificReceiverTypePredicate().negate());
        nodeMatcher = new EquallyLabelledNodeMatcher(config.labelProvider);
        edgeMatcher = new EquallyLabelledEdgeMatcher(new SelAndRepSameLabelProvider(config.labelProvider));
        edgeOrder = new DataEdgeTypePriorityOrder();
        extensionEdgeTypes = config.extensionEdgeTypes;
    }
}
