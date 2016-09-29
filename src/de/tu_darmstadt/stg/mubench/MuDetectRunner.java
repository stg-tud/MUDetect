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
        report(new MuDetect(
                new ProvidedPatternsModel(buildGroums(patternPath)),
                new NoOverlapInstanceFinder(new GreedyInstanceFinder()),
                new MissingElementViolationFactory()
        ).findViolations(buildAUGs(targetPath)), output);
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

    private Collection<AUG> buildAUGs(CodePath path) {
        return new AUGBuilder().build(path.srcPath);
    }

    private void report(List<Violation> violations, DetectorOutput output) {
        for (int rank = 0; rank < violations.size(); rank++) {
            Violation violation =  violations.get(rank);
            Location location = violation.getLocation();
            DetectorFinding finding = output.add(location.getFilePath(), location.getMethodName());
            finding.put("rank", Integer.toString(rank));
            finding.put("pattern_violation", violation.toDotGraph());
            finding.put("target_mapping", violation.toTargetDotGraph());
            finding.put("confidence", Float.toString(violation.getConfidence()));
        }
    }
}
