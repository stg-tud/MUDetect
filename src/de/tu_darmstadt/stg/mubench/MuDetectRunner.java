package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.CodePath;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;
import de.tu_darmstadt.stg.mudetect.*;
import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import egroum.EGroumBuilder;
import egroum.EGroumEdge;
import egroum.EGroumGraph;
import egroum.EGroumNode;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MuDetectRunner extends MuBenchRunner {

    public static void main(String[] args) throws Exception {
        new MuDetectRunner().run(args);
    }

    @Override
    protected void detectOnly(CodePath patternPath, CodePath targetPath, DetectorOutput output) throws Exception {
        report(new MuDetect(
                new ProvidedPatternsModel(buildGroums(patternPath)),
                new GreedyInstanceFinder(),
                new MissingElementViolationStrategy()
        ).findViolations(buildAUGs(targetPath)), output);
    }

    @Override
    protected void mineAndDetect(CodePath trainingAndTargetPath, DetectorOutput output) throws Exception {
        report(new MuDetect(
                new MinedPatternsModel(10, 3, buildGroums(trainingAndTargetPath)),
                new GreedyInstanceFinder(),
                new MissingElementViolationStrategy()
        ).findViolations(buildAUGs(trainingAndTargetPath)), output);
    }

    private Collection<EGroumGraph> buildGroums(CodePath path) {
        return new EGroumBuilder().build(path.srcPath);
    }

    private Collection<AUG> buildAUGs(CodePath path) {
        return buildGroums(path).stream().map(this::toAUG).collect(Collectors.toSet());
    }

    private AUG toAUG(EGroumGraph groum) {
        AUG aug = new AUG(groum.getName(), groum.getFilePath());
        for (EGroumNode node : groum.getNodes()) {
            aug.addVertex(node);
        }
        for (EGroumNode node : groum.getNodes()) {
            for (EGroumEdge edge : node.getInEdges()) {
                aug.addEdge(edge.getSource(), edge.getTarget(), edge);
            }
        }
        return aug;
    }

    private void report(List<Violation> violations, DetectorOutput output) {

    }
}
