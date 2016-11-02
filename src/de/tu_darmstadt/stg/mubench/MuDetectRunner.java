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
import egroum.EGroumBuilder;
import egroum.EGroumGraph;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class MuDetectRunner extends MuBenchRunner {

    public static void main(String[] args) throws Exception {
        new MuDetectRunner().run(args);
    }

    @Override
    protected void detectOnly(CodePath patternPath, CodePath targetPath, DetectorOutput output) throws Exception {
        run(patternPath,
                ProvidedPatternsModel::new,
                targetPath,
                new NoOverlapInstanceFinder(new AlternativeMappingsInstanceFinder()),
                new EverythingViolationFactory(),
                new NoRankingStrategy(),
                output);
    }

    @Override
    protected void mineAndDetect(CodePath trainingAndTargetPath, DetectorOutput output) throws Exception {
        run(trainingAndTargetPath,
                groums -> new MinedPatternsModel(10, 1, groums),
                trainingAndTargetPath,
                new AlternativeMappingsInstanceFinder(),
                new MissingElementViolationFactory(),
                new WeightRankingStrategy(
                        new AverageWeightFunction(
                                new PatternSupportWeightFunction(),
                                new ViolationSupportWeightFunction(),
                                new OverlapWeightFunction())),
                output);
    }

    private void run(CodePath trainingPath,
                     Function<Collection<EGroumGraph>, Model> loadModel,
                     CodePath targetPath,
                     InstanceFinder instanceFinder,
                     ViolationFactory violationFactory,
                     ViolationRankingStrategy rankingStrategy,
                     DetectorOutput output) {

        long startTime = System.currentTimeMillis();
        Collection<EGroumGraph> groums = buildGroums(trainingPath);
        long endTrainingLoadTime = System.currentTimeMillis();
        output.addRunInformation("trainingLoadTime", Long.toString(endTrainingLoadTime - startTime));
        output.addRunInformation("numberOfTrainingExamples", Integer.toString(groums.size()));

        Model model = loadModel.apply(groums);
        long endTrainingTime = System.currentTimeMillis();
        output.addRunInformation("trainingTime", Long.toString(endTrainingTime - endTrainingLoadTime));
        output.addRunInformation("numberOfPatterns", Integer.toString(model.getPatterns().size()));

        Collection<AUG> targets = buildAUGs(targetPath);
        long endDetectionLoadTime = System.currentTimeMillis();
        output.addRunInformation("detectionLoadTime", Long.toString(endDetectionLoadTime - endTrainingTime));
        output.addRunInformation("numberOfTargets", Integer.toString(targets.size()));

        MuDetect detector = new MuDetect(model, instanceFinder, violationFactory, rankingStrategy);
        List<Violation> violations = detector.findViolations(targets);
        long endDetectionTime = System.currentTimeMillis();
        output.addRunInformation("detectionTime", Long.toString(endDetectionTime - endDetectionLoadTime));
        output.addRunInformation("numberOfViolations", Integer.toString(violations.size()));

        report(violations, output);
        long endReportingTime = System.currentTimeMillis();
        output.addRunInformation("reportingTime", Long.toString(endReportingTime - endDetectionTime));
    }

    private Collection<EGroumGraph> buildGroums(CodePath path) {
        return new EGroumBuilder(null /*new String[] {path.classPath}*/).build(path.srcPath);
    }

    private Collection<AUG> buildAUGs(CodePath path) {
        return new AUGBuilder().build(path.srcPath, path.classPath);
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
            finding.put("pattern_support", Integer.toString(violation.getInstance().getPattern().getSupport()));
        }
    }
}
