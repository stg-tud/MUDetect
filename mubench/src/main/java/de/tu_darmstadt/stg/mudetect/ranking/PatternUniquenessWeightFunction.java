package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * As proposed for Jadet by Wasylkowski et al. (2007).
 */
public class PatternUniquenessWeightFunction implements ViolationWeightFunction {
    private Overlaps lastOverlaps;
    private final Map<String, Long> numberOfViolationsByAPI = new HashMap<>();

    @Override
    public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
        APIUsagePattern pattern = violation.getPattern();
        String api = getAPIInvolvedInMostViolations(overlaps, pattern);
        return 1.0 / getNumberOfViolationsInvolvingAPI(overlaps, api);
    }

    private String getAPIInvolvedInMostViolations(Overlaps overlaps, APIUsagePattern pattern) {
        Set<String> patternAPIs = pattern.getAPIs();
        long maxNumberOfViolationsInvolvingAPI = 0;
        String api = null;
        for (String patternAPI : patternAPIs) {
            long numberOfViolationsInvolvingAPI = getNumberOfViolationsInvolvingAPI(overlaps, patternAPI);
            if (numberOfViolationsInvolvingAPI > maxNumberOfViolationsInvolvingAPI) {
                maxNumberOfViolationsInvolvingAPI = numberOfViolationsInvolvingAPI;
                api = patternAPI;
            }
        }
        return api;
    }

    private long getNumberOfViolationsInvolvingAPI(Overlaps overlaps, String patternAPI) {
        validateCache(overlaps);
        if (!numberOfViolationsByAPI.containsKey(patternAPI)) {
            Set<Overlap> violations = overlaps.getViolations();
            numberOfViolationsByAPI.put(patternAPI, violations.stream()
                    .filter(v -> v.getPattern().getAPIs().contains(patternAPI)).count());
        }
        return numberOfViolationsByAPI.get(patternAPI);
    }

    private void validateCache(Overlaps overlaps) {
        if (lastOverlaps != overlaps) {
            numberOfViolationsByAPI.clear();
            lastOverlaps = overlaps;
        }
    }

    @Override
    public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
        APIUsagePattern pattern = violation.getPattern();
        String api = getAPIInvolvedInMostViolations(overlaps, pattern);
        return String.format("1 / %d (%s)", getNumberOfViolationsInvolvingAPI(overlaps, api), api);
    }

    @Override
    public String getId() {
        return "PU";
    }
}