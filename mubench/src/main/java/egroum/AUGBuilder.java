package egroum;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.Edge;
import de.tu_darmstadt.stg.mudetect.aug.Location;
import de.tu_darmstadt.stg.mudetect.aug.Node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AUGBuilder {
    private final AUGConfiguration configuration;

    public AUGBuilder(AUGConfiguration configuration) {
        this.configuration = configuration;
    }

	public Collection<APIUsageExample> build(String sourcePath, String[] classpaths) {
        return new EGroumBuilder(configuration).buildBatch(sourcePath, classpaths).stream()
                .map(AUGBuilder::toAUG).collect(Collectors.toList());
    }

    public Collection<APIUsageExample> build(String source, String basePath, String projectName, String[] classpath) {
        return new EGroumBuilder(configuration).buildGroums(source, basePath, projectName, classpath).stream()
                .map(AUGBuilder::toAUG).collect(Collectors.toList());
    }

    public static APIUsageExample toAUG(EGroumGraph groum) {
        APIUsageExample aug = new APIUsageExample(
                new Location(groum.getProjectName(), groum.getFilePath(), getMethodSignature(groum)));
        Map<EGroumNode, Node> nodeMap = new HashMap<>();
        for (EGroumNode node : groum.getNodes()) {
            Node newNode = convert(node);
            nodeMap.put(node, newNode);
            aug.addVertex(newNode);
        }
        for (EGroumNode node : groum.getNodes()) {
            for (EGroumEdge edge : node.getInEdges()) {
                aug.addEdge(nodeMap.get(edge.getSource()), nodeMap.get(edge.getTarget()), convert(edge));
            }
        }
        return aug;
    }

    private static Edge convert(EGroumEdge edge) {
        throw new UnsupportedOperationException();
    }

    private static Node convert(EGroumNode node) {
        throw new UnsupportedOperationException();
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
