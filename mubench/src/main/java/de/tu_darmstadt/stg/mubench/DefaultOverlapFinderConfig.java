package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mudetect.VeryPrevalentNodePredicate;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import edu.iastate.cs.mudetect.mining.Configuration;

class DefaultOverlapFinderConfig extends AlternativeMappingsOverlapsFinder.Config {
    DefaultOverlapFinderConfig(Configuration config) {
        isStartNode = super.isStartNode.and(new VeryPrevalentNodePredicate());
        nodeMatcher = new EquallyLabelledNodeMatcher(config.nodeToLabel);
        edgeOrder = new DataEdgeTypePriorityOrder();
    }
}
