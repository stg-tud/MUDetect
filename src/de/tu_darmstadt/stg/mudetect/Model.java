package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Pattern;

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
}
