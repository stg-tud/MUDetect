package egroum;

import de.tu_darmstadt.stg.mudetect.aug.*;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import utils.JavaASTUtil;

import java.util.*;
import java.util.stream.Collectors;

import static de.tu_darmstadt.stg.mudetect.aug.ConditionEdge.ConditionType.REPETITION;
import static de.tu_darmstadt.stg.mudetect.aug.ConditionEdge.ConditionType.SELECTION;
import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.*;

public class AUGBuilder {
    private static final Set<String> ASSIGNMENT_OPERATORS = new HashSet<>();
    private static final Set<String> UNARY_OPERATORS = new HashSet<>();
    private static final Set<Integer> LITERAL_AST_NODE_TYPES = new HashSet<>();

    static {
        ASSIGNMENT_OPERATORS.add("+");
        ASSIGNMENT_OPERATORS.add("-");
        ASSIGNMENT_OPERATORS.add("*");
        ASSIGNMENT_OPERATORS.add("/");
        ASSIGNMENT_OPERATORS.add("&");
        ASSIGNMENT_OPERATORS.add("|");
        ASSIGNMENT_OPERATORS.add("^");
        ASSIGNMENT_OPERATORS.add("%");
        ASSIGNMENT_OPERATORS.add(">>");
        ASSIGNMENT_OPERATORS.add("<<");
        ASSIGNMENT_OPERATORS.add(">>>");

        UNARY_OPERATORS.add("!");
        UNARY_OPERATORS.add("+");
        UNARY_OPERATORS.add("-");

        LITERAL_AST_NODE_TYPES.add(ASTNode.BOOLEAN_LITERAL);
        LITERAL_AST_NODE_TYPES.add(ASTNode.CHARACTER_LITERAL);
        LITERAL_AST_NODE_TYPES.add(ASTNode.NULL_LITERAL);
        LITERAL_AST_NODE_TYPES.add(ASTNode.NUMBER_LITERAL);
        LITERAL_AST_NODE_TYPES.add(ASTNode.STRING_LITERAL);
        LITERAL_AST_NODE_TYPES.add(ASTNode.TYPE_LITERAL);
    }

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
                Node source = nodeMap.get(edge.getSource());
                Node target = nodeMap.get(edge.getTarget());
                aug.addEdge(source, target, convert(edge, source, target));
            }
        }
        return aug;
    }

    private static Edge convert(EGroumEdge edge, Node source, Node target) {
        if (edge instanceof EGroumDataEdge) {
            switch (((EGroumDataEdge) edge).getType()) {
                case RECEIVER:
                    return new BaseDataFlowEdge(source, target, RECEIVER);
                case PARAMETER:
                    return new BaseDataFlowEdge(source, target, PARAMETER);
                case ORDER:
                    return new BaseDataFlowEdge(source, target, ORDER);
                case DEFINITION:
                    return new BaseDataFlowEdge(source, target, DEFINITION);
                case QUALIFIER:
                    return new BaseDataFlowEdge(source, target, QUALIFIER);
                case CONDITION:
                    String label = edge.getLabel();
                    switch (label) {
                        case "sel":
                            return new ConditionEdge(source, target, SELECTION);
                        case "rep":
                            return new ConditionEdge(source, target, REPETITION);
                        case "syn":
                            return new BaseControlFlowEdge(source, target, SYNCHRONIZE);
                        default:
                            if (source instanceof ExceptionDataNode) {
                                return new BaseControlFlowEdge(source, target, EXCEPTION_HANDLING);
                            }
                            throw new IllegalArgumentException("unsupported type of condition edge: " + label);
                    }
                case THROW:
                    return new BaseControlFlowEdge(source, target, THROW);
                case FINALLY:
                    return new BaseControlFlowEdge(source, target, FINALLY);
                case CONTAINS:
                    return new BaseControlFlowEdge(source, target, CONTAINS);
            }
        }
        throw new IllegalArgumentException("unsupported edge type: " + edge.getLabel());
    }

    private static Node convert(EGroumNode node) {
        if (node instanceof EGroumDataNode) {
            if (((EGroumDataNode) node).isException()) {
                return new ExceptionDataNode(node.getDataType(), node.getDataName());
            } else if (node.astNodeType == ASTNode.SIMPLE_NAME) {
                return new VariableNode(node.getDataType(), node.getDataName());
            } else if (LITERAL_AST_NODE_TYPES.contains(node.astNodeType)) {
                return new LiteralNode(node.getDataType(), node.getDataName());
            } else if (node.getLabel().endsWith("()")) {
                // encoding of the methods of anonymous class instances
                return new AnonymousClassMethod(node.getLabel());
            } else {
                return new ObjectDataNode(node.getDataType());
            }
        } else if (node instanceof EGroumActionNode) {
            String label = node.getLabel();
            if (label.endsWith("()")) {
                // TODO split declaring type and signature
                return new MethodCallNode(label);
            } else if (label.endsWith("<init>")) {
                // TODO split declaring type and signature
                return new ConstructorCallNode(label);
            } else if (label.startsWith("{") && label.endsWith("}")) {
                return new ArrayCreationNode(label.substring(1, label.length() - 1));
            } else if (label.endsWith("<cast>")) {
                return new CastNode(label.split("\\.")[0]);
            } else if (JavaASTUtil.infixExpressionLables.containsValue(label)) {
                // TODO capture non-generalized operator
                return new InfixOperatorNode(label);
            } else if (ASSIGNMENT_OPERATORS.contains(label)) {
                // this happens because we transform operators such as += and -= into and = and the respective
                // operation, but to not apply the operator abstraction afterwards, i.e., this is actually a bug
                // in the transformation.
                // TODO ensure consistent handling of operators
                return new InfixOperatorNode(label);
            } else if (UNARY_OPERATORS.contains(label)) {
                return new UnaryOperatorNode(label);
            } else if (label.equals("=")) {
                return new AssignmentNode();
            } else if (label.equals("<nullcheck>")) {
                return new NullCheckNode();
            } else if (label.equals("break")) {
                return new BreakNode();
            } else if (label.equals("continue")) {
                return new ContinueNode();
            } else if (label.equals("return")) {
                return new ReturnNode();
            } else if (label.equals("throw")) {
                return new ThrowNode();
            }
        }
        throw new IllegalArgumentException("unsupported node: " + node);
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
