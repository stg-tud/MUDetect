package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.CodePath;
import de.tu_darmstadt.stg.mubench.cli.DetectorFinding;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;
import de.tu_darmstadt.stg.mudetect.*;
import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Location;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import egroum.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MuDetectRunner extends MuBenchRunner {

    public static void main(String[] args) throws Exception {
        new MuDetectRunner().run(args);
    }

    @Override
    protected void detectOnly(CodePath patternPath, CodePath targetPath, DetectorOutput output) throws Exception {
        Collection<EGroumGraph> patterns = buildGroums(patternPath);
        Collection<AUG> targets = buildAUGs(targetPath);

        List<Violation> violations = new MuDetect(
                new ProvidedPatternsModel(patterns),
                new GreedyInstanceFinder(),
                new MissingElementViolationFactory()
        ).findViolations(targets);

        if (violations.isEmpty()) {
            patterns.stream().map(AUGBuilder::toAUG).forEach(pattern -> {
                for (AUG target : targets) {
                    violations.add(new Violation(new Instance(pattern, target, new HashMap<>(), new HashMap<>()), -1));
                }
            });
        }

        report(violations, output);
    }

    @Override
    protected void mineAndDetect(CodePath trainingAndTargetPath, DetectorOutput output) throws Exception {
        report(new MuDetect(
                new MinedPatternsModel(10, 3, buildGroums(trainingAndTargetPath)),
                new GreedyInstanceFinder(),
                new MissingElementViolationFactory()
        ).findViolations(buildAUGs(trainingAndTargetPath)), output);
    }

    private Collection<EGroumGraph> buildGroums(CodePath path) {
        return new EGroumBuilder().build(path.srcPath);
    }

    private Collection<AUG> buildAUGs(CodePath trainingAndTargetPath) {
        return new AUGBuilder().build(trainingAndTargetPath.srcPath);
    }

    private void report(List<Violation> violations, DetectorOutput output) {
        for (int rank = 0; rank < violations.size(); rank++) {
            Violation violation =  violations.get(rank);
            Location location = violation.getLocation();
            DetectorFinding finding = output.add(location.getFilePath(), location.getMethodName());
            finding.put("rank", Integer.toString(rank));
            finding.put("pattern_violation", violation.toDotGraph());
            finding.put("confidence", Float.toString(violation.getConfidence()));
        }
    }
}
