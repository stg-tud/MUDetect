package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Pattern;

import java.util.Set;

public interface Model {
    Set<Pattern> getPatterns();

    default int getMaxPatternSupport(int nodeCount) {
        int maxSupport = 0;
        for (Pattern pattern : getPatterns()) {
            if (pattern.getNodeCount() == nodeCount) {
                int support = pattern.getSupport();
                if (support > maxSupport) {
                    maxSupport = support;
                }
            }
        }
        return maxSupport;
    }
}
