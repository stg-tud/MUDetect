package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;

import java.util.Collection;
import java.util.stream.Collectors;

public class AUGBuilder {
    public Collection<AUG> build(String path) {
        return new EGroumBuilder().build(path).stream().map(AUGBuilder::toAUG).collect(Collectors.toSet());
    }

    public static AUG toAUG(EGroumGraph groum) {
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
        // name comes like "DeclaringType.methodName#Parameter1#Parameter2# or "DeclaringType.noParamMethod#"
        String name = graph.getName();
        // remove "DeclaringType" and trailing "#"
        name = name.substring(name.indexOf('.') + 1, name.length() - 1);
        if (!name.contains("#")) {
            name += "(";
        } else {
            // replace first "#" by "("
            name = name.replaceFirst("#", "(");
        }
        // replace remaining "#" by ", " and close parameter list
        name = name.replace("#", ", ") + ")";
        // result is "methodName(Parameter1, Parameter2)
        return name;
    }
}
