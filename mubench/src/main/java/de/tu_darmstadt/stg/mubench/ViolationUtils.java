package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.DetectorFinding;
import de.tu_darmstadt.stg.mudetect.dot.ViolationDotExporter;
import de.tu_darmstadt.stg.mudetect.model.Location;
import de.tu_darmstadt.stg.mudetect.model.Violation;

import java.util.Set;
import java.util.stream.Collectors;

class ViolationUtils {
    private static final ViolationDotExporter violationDotExporter = new ViolationDotExporter();

    static DetectorFinding toFinding(Violation violation) {
        Location location = violation.getLocation();
        DetectorFinding finding = new DetectorFinding(location.getFilePath(), location.getMethodName());
        finding.put("pattern_violation", violationDotExporter.toDotGraph(violation));
        finding.put("target_environment_mapping", violationDotExporter.toTargetEnvironmentDotGraph(violation));
        finding.put("confidence", violation.getConfidence());
        finding.put("pattern_support", violation.getOverlap().getPattern().getSupport());
        finding.put("confidence_string", violation.getConfidenceString());
        finding.put("pattern_examples", getPatternInstanceLocations(violation));
        finding.put("startline", getStartLine(violation));
        return finding;
    }

    private static Set<String> getPatternInstanceLocations(Violation violation) {
        return violation.getOverlap().getPattern().getExampleLocations().stream()
                .map(Object::toString).map(loc -> loc.split("checkouts/")[1]).distinct().limit(5)
                .collect(Collectors.toSet());
    }

    private static int getStartLine(Violation violation) {
        return violation.getOverlap().getMappedTargetNodes().stream()
                .mapToInt(node -> node.getSourceLineNumber().orElse(Integer.MAX_VALUE))
                .min().orElse(0);
    }

}
