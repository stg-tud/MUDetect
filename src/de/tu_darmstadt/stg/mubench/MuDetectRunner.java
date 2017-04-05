package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.CodePath;
import de.tu_darmstadt.stg.mubench.cli.DetectorFinding;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;
import de.tu_darmstadt.stg.mudetect.*;
import de.tu_darmstadt.stg.mudetect.dot.ViolationDotExporter;
import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Location;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mudetect.ranking.*;
import egroum.AUGBuilder;
import egroum.AUGConfiguration;
import egroum.EGroumBuilder;
import egroum.EGroumGraph;
import mining.Configuration;
import de.tu_darmstadt.stg.mudetect.mining.MinedPatternsModel;
import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.mining.ProvidedPatternsModel;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class MuDetectRunner extends MuBenchRunner {

    public static void main(String[] args) throws Exception {
        new MuDetectRunner().run(args);
    }

    @Override
    protected void detectOnly(CodePath patternPath, CodePath targetPath, DetectorOutput output) throws Exception {
        run(new AUGConfiguration(),
                patternPath,
                ProvidedPatternsModel::new,
                targetPath,
                new EmptyOverlapsFinder(new AlternativeMappingsOverlapsFinder(new OverlapRatioPredicate(0.5))),
                new EverythingViolationFactory(),
                new NoRankingStrategy(),
                output);
    }

    @Override
    protected void mineAndDetect(CodePath trainingAndTargetPath, DetectorOutput output) throws Exception {
        run(new AUGConfiguration(),
                trainingAndTargetPath,
                groums -> new MinedPatternsModel(new Configuration() {{
                    minPatternSupport = 10;
                    disable_system_out = true;
                    outputPath = getPatternOutputPath();
                }}, groums),
                trainingAndTargetPath,
                new AlternativeMappingsOverlapsFinder(new OverlapRatioPredicate(0.5)),
                new MissingElementViolationFactory(),
                new WeightRankingStrategy(
                        new AverageWeightFunction(
                                new PatternSupportWeightFunction(),
                                new PatternViolationsWeightFunction(),
                                new ViolationSupportWeightFunction(),
                                new OverlapWithEdgesToMissingNodesWeightFunction())),
                output);
    }

    /**
     * Start run with --java-options Dmudetect.mining.outputpath="/path/to/write/patterns/to"
     */
    private static String getPatternOutputPath() {
        return System.getProperty("mudetect.mining.outputpath");
    }

    private void run(AUGConfiguration configuration,
                     CodePath trainingPath,
                     Function<Collection<EGroumGraph>, Model> loadModel,
                     CodePath targetPath,
                     OverlapsFinder overlapsFinder,
                     ViolationFactory violationFactory,
                     ViolationRankingStrategy rankingStrategy,
                     DetectorOutput output) {

        long startTime = System.currentTimeMillis();
        Collection<EGroumGraph> groums = buildGroums(trainingPath, configuration);
        long endTrainingLoadTime = System.currentTimeMillis();
        output.addRunInformation("trainingLoadTime", Long.toString(endTrainingLoadTime - startTime));
        output.addRunInformation("numberOfTrainingExamples", Integer.toString(groums.size()));
        System.out.println("Number of training examples = " + groums.size());

        Model model = loadModel.apply(groums);
        long endTrainingTime = System.currentTimeMillis();
        output.addRunInformation("trainingTime", Long.toString(endTrainingTime - endTrainingLoadTime));
        output.addRunInformation("numberOfPatterns", Integer.toString(model.getPatterns().size()));
        System.out.println("Number of patterns = " + model.getPatterns().size());
        output.addRunInformation("maxPatternSupport", Integer.toString(model.getMaxPatternSupport()));
        System.out.println("Maximum pattern support = " + model.getMaxPatternSupport());

        Collection<AUG> targets = buildAUGs(targetPath, configuration);
        long endDetectionLoadTime = System.currentTimeMillis();
        output.addRunInformation("detectionLoadTime", Long.toString(endDetectionLoadTime - endTrainingTime));
        output.addRunInformation("numberOfTargets", Integer.toString(targets.size()));
        System.out.println("Number of targets = " + targets.size());

        MuDetect detector = new MuDetect(model, overlapsFinder, violationFactory, rankingStrategy);
        List<Violation> violations = detector.findViolations(targets);
        long endDetectionTime = System.currentTimeMillis();
        output.addRunInformation("detectionTime", Long.toString(endDetectionTime - endDetectionLoadTime));
        output.addRunInformation("numberOfViolations", Integer.toString(violations.size()));
        System.out.println("Number of violations = " + violations.size());
        output.addRunInformation("numberOfExploredAlternatives", Long.toString(AlternativeMappingsOverlapsFinder.numberOfExploredAlternatives));
        System.out.println("Number of explored alternatives = " + AlternativeMappingsOverlapsFinder.numberOfExploredAlternatives);

        report(violations, output);
        long endReportingTime = System.currentTimeMillis();
        output.addRunInformation("reportingTime", Long.toString(endReportingTime - endDetectionTime));
    }

    private Collection<EGroumGraph> buildGroums(CodePath path, AUGConfiguration configuration) {
        return new EGroumBuilder(configuration).buildBatch(path.srcPath, null);
    }

    private Collection<AUG> buildAUGs(CodePath path, AUGConfiguration configuration) {
        return new AUGBuilder(configuration).build(path.srcPath, null);
    }

    private void report(List<Violation> violations, DetectorOutput output) {
        ViolationDotExporter violationDotExporter = new ViolationDotExporter();
        for (int rank = 0; rank < violations.size(); rank++) {
            Violation violation =  violations.get(rank);
            Location location = violation.getLocation();
            DetectorFinding finding = output.add(location.getFilePath(), location.getMethodName());
            finding.put("rank", Integer.toString(rank));
            finding.put("pattern_violation", violationDotExporter.toDotGraph(violation));
            finding.put("target_environment_mapping", violationDotExporter.toTargetEnvironmentDotGraph(violation));
            finding.put("confidence", Float.toString(violation.getConfidence()));
            finding.put("pattern_support", Integer.toString(violation.getOverlap().getPattern().getSupport()));
            finding.put("confidence_string", violation.getConfidenceString());
        }
    }
}
