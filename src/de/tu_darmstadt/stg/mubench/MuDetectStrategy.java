package de.tu_darmstadt.stg.mubench;

import com.google.common.collect.Multiset;
import de.tu_darmstadt.stg.mubench.cli.*;
import de.tu_darmstadt.stg.mudetect.*;
import de.tu_darmstadt.stg.mudetect.dot.ViolationDotExporter;
import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Location;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mustudies.UsageUtils;
import de.tu_darmstadt.stg.yaml.YamlObject;
import egroum.AUGBuilder;
import egroum.EGroumGraph;
import mining.Configuration;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

abstract class MuDetectStrategy implements DetectionStrategy {

    private final ViolationDotExporter violationDotExporter = new ViolationDotExporter();

    abstract Collection<EGroumGraph> loadTrainingExamples(DetectorArgs args) throws IOException;

    abstract Miner createMiner();

    private Collection<AUG> loadDetectionTargets(DetectorArgs args) throws IOException {
        return new AUGBuilder(new DefaultAUGConfiguration()).build(args.getTargetPath().srcPath, args.getDependencyClassPath());
    }

    abstract MuDetect createDetector(Model model);

    @Override
    public DetectorOutput detectViolations(DetectorArgs args) throws Exception {
        DetectorOutput.Builder output = createOutput();

        long startTime = System.currentTimeMillis();
        Collection<EGroumGraph> groums = loadTrainingExamples(args);
        long endTrainingLoadTime = System.currentTimeMillis();
        output.withRunInfo("trainingLoadTime", endTrainingLoadTime - startTime);
        output.withRunInfo("numberOfTrainingExamples", groums.size());

        Model model = createMiner().mine(groums);
        long endTrainingTime = System.currentTimeMillis();
        output.withRunInfo("trainingTime", endTrainingTime - endTrainingLoadTime);
        output.withRunInfo("numberOfPatterns", model.getPatterns().size());
        output.withRunInfo("maxPatternSupport", model.getMaxPatternSupport());

        Collection<AUG> targets = loadDetectionTargets(args);
        long endDetectionLoadTime = System.currentTimeMillis();
        output.withRunInfo("numberOfUsages", getTypeUsageCounts(targets));
        output.withRunInfo("detectionLoadTime", endDetectionLoadTime - endTrainingTime);
        output.withRunInfo("numberOfTargets", targets.size());

        List<Violation> violations = createDetector(model).findViolations(targets);
        long endDetectionTime = System.currentTimeMillis();
        output.withRunInfo("detectionTime", endDetectionTime - endDetectionLoadTime);
        output.withRunInfo("numberOfViolations", violations.size());
        output.withRunInfo("numberOfExploredAlternatives", AlternativeMappingsOverlapsFinder.numberOfExploredAlternatives);

        return output.withFindings(violations, this::toFinding);
    }

    private YamlObject getTypeUsageCounts(Collection<AUG> targets) {
        YamlObject object = new YamlObject();
        for (Multiset.Entry<String> entry : UsageUtils.countNumberOfUsagesPerType(targets).entrySet()) {
            object.put(entry.getElement(), entry.getCount());
        }
        return object;
    }

    private DetectorFinding toFinding(Violation violation) {
        Location location = violation.getLocation();
        DetectorFinding finding = new DetectorFinding(location.getFilePath(), location.getMethodName());
        finding.put("pattern_violation", violationDotExporter.toDotGraph(violation));
        finding.put("target_environment_mapping", violationDotExporter.toTargetEnvironmentDotGraph(violation));
        finding.put("confidence", violation.getConfidence());
        finding.put("pattern_support", violation.getOverlap().getPattern().getSupport());
        finding.put("confidence_string", violation.getConfidenceString());
        finding.put("pattern_examples", getPatternInstanceLocations(violation));
        return finding;
    }

    private Set<String> getPatternInstanceLocations(Violation violation) {
        return violation.getOverlap().getPattern().getExampleLocations().stream()
                .map(Object::toString).map(loc -> loc.split("checkouts/")[1]).distinct().limit(5)
                .collect(Collectors.toSet());
    }
}
