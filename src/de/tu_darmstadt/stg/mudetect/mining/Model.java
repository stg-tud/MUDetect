package de.tu_darmstadt.stg.mudetect.mining;

import java.util.Set;

public interface Model {
    Set<Pattern> getPatterns();

    default int getMaxPatternSupport() {
        int maxSupport = 0;
        for (Pattern pattern : getPatterns()) {
            int support = pattern.getSupport();
            if (support > maxSupport) {
                maxSupport = support;
            }
        }
        return maxSupport;
    }

    default int getMaxPatternSupport(int nodeCount) {
        int maxSupport = 0;
        for (Pattern pattern : getPatterns()) {
            if (pattern.getNodeSize() == nodeCount) {
                int support = pattern.getSupport();
                if (support > maxSupport) {
                    maxSupport = support;
                }
            }
        }
        return maxSupport;
    }
}
