package de.tu_darmstadt.stg.mudetect.mining;

import egroum.EGroumDataNode;
import egroum.EGroumGraph;
import egroum.EGroumNode;
import mining.Configuration;

import java.util.*;

class APIWiseModel {
    private Map<String, Model> modelByAPI = new HashMap<>();

    public APIWiseModel(Configuration configuration, Collection<EGroumGraph> groums) {
        Map<String, Collection<EGroumGraph>> apiToUsageMapping = getAPItoUsageMapping(groums);
        for (String api : apiToUsageMapping.keySet()) {
            Collection<EGroumGraph> examples = apiToUsageMapping.get(api);
            MinedPatternsModel apiModel = new MinedPatternsModel(configuration, examples);
            if (!apiModel.getPatterns().isEmpty()) {
                modelByAPI.put(api, apiModel);
            }
        }
    }

    public Set<String> getAPIs() {
        return modelByAPI.keySet();
    }

    private static Map<String, Collection<EGroumGraph>> getAPItoUsageMapping(Collection<EGroumGraph> groums) {
        Map<String, Collection<EGroumGraph>> apiToUsageMapping = new HashMap<>();
        for (EGroumGraph groum : groums) {
            Set<String> apis = getAPIs(groum);
            for (String api : apis) {
                if (!apiToUsageMapping.containsKey(api)) {
                    apiToUsageMapping.put(api, new HashSet<>());
                }
                apiToUsageMapping.get(api).add(groum);
            }
        }
        return apiToUsageMapping;
    }

    private static Set<String> getAPIs(EGroumGraph groum) {
        Set<String> apis = new HashSet<>();
        for (EGroumNode node : groum.getNodes()) {
            if (isAPI(node)) apis.add(node.getLabel());
        }
        return apis;
    }

    private static boolean isAPI(EGroumNode node) {
        if (node instanceof EGroumDataNode) {
            String label = node.getLabel();
            switch (label) {
                case "int":
                case "long":
                case "float":
                case "double":
                case "short":
                case "boolean":
                    return false;
                default:
                    return !label.endsWith("[]");
            }
        }
        return false;
    }
}
