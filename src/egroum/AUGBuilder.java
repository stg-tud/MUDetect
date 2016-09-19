package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;

import java.util.Collection;
import java.util.stream.Collectors;

public class AUGBuilder {
    public Collection<AUG> build(String path) {
        return new EGroumBuilder().build(path).stream().map(this::toAUG).collect(Collectors.toSet());
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
}
