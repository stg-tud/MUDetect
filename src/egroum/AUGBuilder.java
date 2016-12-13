package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;

import java.util.Collection;
import java.util.stream.Collectors;

public class AUGBuilder {
	public Collection<AUG> build(String sourcePath, String[] classpaths) {
        return new EGroumBuilder().buildBatch(sourcePath, classpaths).stream()
                .map(AUGBuilder::toAUG).collect(Collectors.toSet());
    }

    public static AUG toAUG(EGroumGraph groum) {
        AUG aug = new AUG(getMethodSignature(groum), groum.getFilePath());
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

    public static String getMethodSignature(EGroumGraph graph) {
        // Examples of graph names are:
        // - C.noParamMethod#
        // - C.method#ParamType1#ParamType2#
        // - C.method#A.B#
        // - C.I.method#
        String[] parts = graph.getName().split("#", 2);
        return toMethodName(parts[0]) + toParameterList(parts[1]);
    }

    private static String toMethodName(String qualifiedMethodName) {
        return qualifiedMethodName.substring(qualifiedMethodName.lastIndexOf(".") + 1);
    }

    private static String toParameterList(String parameterList) {
        String[] parameters = parameterList.split("#");
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = toSimpleTypeName(parameters[i]);
        }
        return "(" + String.join(", ", (CharSequence[]) parameters) + ")";
    }

    private static String toSimpleTypeName(String typeName) {
        return typeName.substring(typeName.lastIndexOf(".") + 1);
    }
}
