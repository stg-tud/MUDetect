package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import mining.Configuration;

class DefaultOverlapFinderConfig extends AlternativeMappingsOverlapsFinder.Config {
    DefaultOverlapFinderConfig(Configuration config){
        nodeMatcher = new EquallyLabelledNodeMatcher(config.nodeToLabel);
        edgeOrder = new DataEdgeTypePriorityOrder();
    }
}
