package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;

import java.util.Collection;
import java.util.stream.Collectors;

public class AUGBuilder {
    public Collection<AUG> build(String path) {
        return new EGroumBuilder().build(path).stream().map(AUGBuilder::toAUG).collect(Collectors.toSet());
    }

    private static AUG toAUG(EGroumGraph groum) {
        AUG aug = new AUG(getMethodName(groum), groum.getFilePath());
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

    public static String getMethodName(EGroumGraph graph) {
        // name comes like "DeclaringType.methodName#Parameter1#Parameter2#
        String name = graph.getName();
        // Cut off "DeclaringType"
        name = name.substring(name.indexOf('.') + 1);
        // replace first "#" by "(" and last by ")"
        name = name.replaceFirst("#", "(").substring(0, name.length() - 1) + ")";
        // replace remaining "#" by ", "
        name = name.replace("#", ", ");
        // result is "methodName(Parameter1, Parameter2)
        return name;
    }
}
