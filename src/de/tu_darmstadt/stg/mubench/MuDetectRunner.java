package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.*;
import de.tu_darmstadt.stg.mudetect.*;
import de.tu_darmstadt.stg.mudetect.dot.ViolationDotExporter;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.matcher.SubtypeNodeMatcher;
import de.tu_darmstadt.stg.mudetect.mining.MinedPatternsModel;
import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.mining.ProvidedPatternsModel;
import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Location;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mudetect.ranking.*;
import de.tu_darmstadt.stg.mudetect.typehierarchy.TargetSrcTypeHierarchy;
import egroum.AUGBuilder;
import egroum.AUGConfiguration;
import egroum.EGroumBuilder;
import egroum.EGroumGraph;
import mining.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MuDetectRunner extends MuBenchRunner {

    public static void main(String[] args) throws Exception {
        new MuDetectRunner().run(args);
    }

    @Override
    protected void detectOnly(DetectorArgs args, DetectorOutput output) throws Exception {
        run(getAUGConfiguration(),
                args.getPatternPath(),
                ProvidedPatternsModel::new,
                args.getTargetPath(),
                args.getDependencyClassPath(),
                new EmptyOverlapsFinder(new AlternativeMappingsOverlapsFinder(new AlternativeMappingsOverlapsFinder.Config() {{
                    nodeMatcher = new EquallyLabelledNodeMatcher();
                }})),
                new EverythingViolationFactory(),
                new NoRankingStrategy(),
                output);
    }

    @Override
    protected void mineAndDetect(DetectorArgs args, DetectorOutput output) throws Exception {
        run(getAUGConfiguration(),
                args.getTargetPath(),
                groums -> new MinedPatternsModel(new Configuration() {{
                    minPatternSupport = 10;
                    minPatternCalls = 2;
                    disableSystemOut = true;
                    outputPath = getPatternOutputPath();
                }}, groums),
                args.getTargetPath(),
                args.getDependencyClassPath(),
                new AlternativeMappingsOverlapsFinder(
                        new AlternativeMappingsOverlapsFinder.Config() {{
                            nodeMatcher = new SubtypeNodeMatcher(TargetSrcTypeHierarchy.build(
                                    args.getTargetPath().srcPath,
                                    args.getDependencyClassPath())).or(new EquallyLabelledNodeMatcher());
                        }}),
                new MissingElementViolationFactory(),
                new WeightRankingStrategy(
                        new AverageWeightFunction(
                                new PatternSupportWeightFunction(),
                                new PatternViolationsWeightFunction(),
                                new ViolationSupportWeightFunction(),
                                new OverlapWithoutEdgesToMissingNodesWeightFunction(
                                        new ConstantNodeWeightFunction()
                                ))),
                output);
    }

    private static AUGConfiguration getAUGConfiguration() {
        return new AUGConfiguration() {{
            collapseIsomorphicSubgraphs = true;
            collapseTemporaryDataNodes = false;
            collapseTemporaryDataNodesIncomingToControlNodes = true;
            encodeUnaryOperators = false;
            encodeConditionalOperators = false;
            removeImplementationCode = 2;
            groum = false;
            minStatements = 0;
        }};
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
                     String[] dependenciesClassPath,
                     OverlapsFinder overlapsFinder,
                     ViolationFactory violationFactory,
                     ViolationRankingStrategy rankingStrategy,
                     DetectorOutput output) {

        long startTime = System.currentTimeMillis();
        Collection<EGroumGraph> groums = buildGroums(trainingPath, dependenciesClassPath, configuration);
        long endTrainingLoadTime = System.currentTimeMillis();
        output.addRunInformation("trainingLoadTime", Long.toString(endTrainingLoadTime - startTime));
        output.addRunInformation("numberOfTrainingExamples", Integer.toString(groums.size()));

        Model model = loadModel.apply(groums);
        long endTrainingTime = System.currentTimeMillis();
        output.addRunInformation("trainingTime", Long.toString(endTrainingTime - endTrainingLoadTime));
        output.addRunInformation("numberOfPatterns", Integer.toString(model.getPatterns().size()));
        output.addRunInformation("maxPatternSupport", Integer.toString(model.getMaxPatternSupport()));

        Collection<AUG> targets = buildAUGs(targetPath, dependenciesClassPath, configuration);
        long endDetectionLoadTime = System.currentTimeMillis();
        output.addRunInformation("detectionLoadTime", Long.toString(endDetectionLoadTime - endTrainingTime));
        output.addRunInformation("numberOfTargets", Integer.toString(targets.size()));

        MuDetect detector = new MuDetect(model, overlapsFinder, violationFactory, rankingStrategy);
        List<Violation> violations = detector.findViolations(targets);
        long endDetectionTime = System.currentTimeMillis();
        output.addRunInformation("detectionTime", Long.toString(endDetectionTime - endDetectionLoadTime));
        output.addRunInformation("numberOfViolations", Integer.toString(violations.size()));
        output.addRunInformation("numberOfExploredAlternatives", Long.toString(AlternativeMappingsOverlapsFinder.numberOfExploredAlternatives));

        report(violations, output);
        long endReportingTime = System.currentTimeMillis();
        output.addRunInformation("reportingTime", Long.toString(endReportingTime - endDetectionTime));
    }

    private Collection<EGroumGraph> buildGroums(CodePath path, String[] dependenciesClassPath, AUGConfiguration configuration) {
        return new EGroumBuilder(configuration).buildBatch(path.srcPath, dependenciesClassPath);
    }

    private Collection<AUG> buildAUGs(CodePath path, String[] dependenciesClassPath, AUGConfiguration configuration) {
        return new AUGBuilder(configuration).build(path.srcPath, dependenciesClassPath);
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
            finding.put("confidence", Double.toString(violation.getConfidence()));
            finding.put("pattern_support", Integer.toString(violation.getOverlap().getPattern().getSupport()));
            finding.put("confidence_string", violation.getConfidenceString());
            finding.put("pattern_examples", violation.getOverlap().getPattern().getExampleLocations().stream()
                    .map(Object::toString).map(loc -> loc.split("checkouts/")[1]).distinct().limit(5)
                    .collect(Collectors.toSet()));
        }
    }
}
