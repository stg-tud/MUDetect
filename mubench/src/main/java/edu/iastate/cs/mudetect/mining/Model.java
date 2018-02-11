package edu.iastate.cs.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;

import java.util.Set;

public interface Model {
    Set<APIUsagePattern> getPatterns();

    default int getMaxPatternSupport() {
        int maxSupport = 0;
        for (APIUsagePattern pattern : getPatterns()) {
            int support = pattern.getSupport();
            if (support > maxSupport) {
                maxSupport = support;
            }
        }
        return maxSupport;
    }

    default int getMaxPatternSupport(int nodeCount) {
        int maxSupport = 0;
        for (APIUsagePattern pattern : getPatterns()) {
            if (pattern.vertexSet().size() == nodeCount) {
                int support = pattern.getSupport();
                if (support > maxSupport) {
                    maxSupport = support;
                }
            }
        }
        return maxSupport;
    }
}
