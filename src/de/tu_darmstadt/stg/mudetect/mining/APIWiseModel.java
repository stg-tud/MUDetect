package de.tu_darmstadt.stg.mudetect.mining;

import egroum.EGroumGraph;
import mining.Configuration;

import java.util.*;

class APIWiseModel {

    private final Map<String, Set<Pattern>> apiToPatternMapping;

    public APIWiseModel(Configuration configuration, Collection<EGroumGraph> groums) {
        Set<Pattern> patterns = new MinedPatternsModel(configuration, groums).getPatterns();
        apiToPatternMapping = getAPItoPatternMapping(patterns);
    }

    public Set<String> getPatternAPIs() {
        return apiToPatternMapping.keySet();
    }

    private static Map<String, Set<Pattern>> getAPItoPatternMapping(Set<Pattern> patterns) {
        Map<String, Set<Pattern>> apiToUsageMapping = new HashMap<>();
        for (Pattern pattern : patterns) {
            Set<String> apis = pattern.getAPIs();
            for (String api : apis) {
                if (!apiToUsageMapping.containsKey(api)) {
                    apiToUsageMapping.put(api, new HashSet<>());
                }
                apiToUsageMapping.get(api).add(pattern);
            }
        }
        return apiToUsageMapping;
    }
}
