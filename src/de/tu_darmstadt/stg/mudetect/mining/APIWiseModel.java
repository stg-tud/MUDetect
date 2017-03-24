package de.tu_darmstadt.stg.mudetect.mining;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import egroum.EGroumGraph;
import egroum.EGroumNode;
import mining.Configuration;

import java.util.*;
import java.util.stream.Collectors;

class APIWiseModel {

    private final Multiset<String> apiOccurrences;
    private final Map<String, Set<Pattern>> apiToPatternMapping;

    public APIWiseModel(Configuration configuration, Collection<EGroumGraph> groums) {
        apiOccurrences = getAPIOccurrences(groums);
        Set<Pattern> patterns = new MinedPatternsModel(configuration, groums).getPatterns();
        apiToPatternMapping = getAPItoPatternMapping(patterns);
    }

    public Set<String> getExampleAPIs() {
        return apiOccurrences.elementSet();
    }

    public int getNumberOfExamples(String api) {
        return apiOccurrences.count(api);
    }

    public Set<String> getPatternAPIs() {
        return apiToPatternMapping.keySet();
    }

    private Multiset<String> getAPIOccurrences(Collection<EGroumGraph> groums) {
        Multiset<String> apiOccurrences = HashMultiset.create();
        for (EGroumGraph groum : groums) {
            apiOccurrences.addAll(groum.getNodes().stream()
                    .filter(EGroumNode::isAPI).map(EGroumNode::getLabel).collect(Collectors.toList()));
        }
        return apiOccurrences;
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
