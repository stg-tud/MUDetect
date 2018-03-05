package de.tu_darmstadt.stg.mudetect.aug.persistence;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.*;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.*;
import de.tu_darmstadt.stg.mudetect.aug.model.data.*;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.DefinitionEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ParameterEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.QualifierEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ReceiverEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.AUGDotExporter;
import de.tu_darmstadt.stg.mudetect.aug.visitors.NodeVisitor;
import org.jgrapht.ext.ComponentAttributeProvider;

import java.util.HashMap;
import java.util.Map;

public class PersistenceAUGDotExporter extends AUGDotExporter {
    static final String SOURCE_LINE_NUMBER = "l";
    static final String DECLARING_TYPE = "t";
    static final String METHOD_SIGNATURE = "s";
    static final String TARGET_TYPE = "t";
    static final String CATCH_TYPE = "t";
    static final String OPERATOR = "o";
    static final String DATA_NAME = "n";
    static final String DATA_TYPE = "t";
    static final String DATA_VALUE = "v";

    static final BiMap<Class, String> AUG_ELEMENT_TYPE_TO_LABEL = HashBiMap.create();

    static {
        AUG_ELEMENT_TYPE_TO_LABEL.put(ArrayAccessNode.class, "A");
        AUG_ELEMENT_TYPE_TO_LABEL.put(ArrayAssignmentNode.class, "AA");
        AUG_ELEMENT_TYPE_TO_LABEL.put(ArrayCreationNode.class, "AC");
        AUG_ELEMENT_TYPE_TO_LABEL.put(AssignmentNode.class, "As");
        AUG_ELEMENT_TYPE_TO_LABEL.put(BreakNode.class, "B");
        AUG_ELEMENT_TYPE_TO_LABEL.put(CastNode.class, "C");
        AUG_ELEMENT_TYPE_TO_LABEL.put(CatchNode.class, "Ca");
        AUG_ELEMENT_TYPE_TO_LABEL.put(ConstructorCallNode.class, "I");
        AUG_ELEMENT_TYPE_TO_LABEL.put(ContinueNode.class, "Co");
        AUG_ELEMENT_TYPE_TO_LABEL.put(InfixOperatorNode.class, "IO");
        AUG_ELEMENT_TYPE_TO_LABEL.put(MethodCallNode.class, "MC");
        AUG_ELEMENT_TYPE_TO_LABEL.put(NullCheckNode.class, "N");
        AUG_ELEMENT_TYPE_TO_LABEL.put(ReturnNode.class, "Rt");
        AUG_ELEMENT_TYPE_TO_LABEL.put(SuperConstructorCallNode.class, "SI");
        AUG_ELEMENT_TYPE_TO_LABEL.put(SuperMethodCallNode.class, "SC");
        AUG_ELEMENT_TYPE_TO_LABEL.put(ThrowNode.class, "T");
        AUG_ELEMENT_TYPE_TO_LABEL.put(TypeCheckNode.class, "TC");
        AUG_ELEMENT_TYPE_TO_LABEL.put(UnaryOperatorNode.class, "UO");

        AUG_ELEMENT_TYPE_TO_LABEL.put(AnonymousClassMethodNode.class, "ACM");
        AUG_ELEMENT_TYPE_TO_LABEL.put(AnonymousObjectNode.class, "AO");
        AUG_ELEMENT_TYPE_TO_LABEL.put(ConstantNode.class, "CN");
        AUG_ELEMENT_TYPE_TO_LABEL.put(ExceptionNode.class, "E");
        AUG_ELEMENT_TYPE_TO_LABEL.put(LiteralNode.class, "L");
        AUG_ELEMENT_TYPE_TO_LABEL.put(VariableNode.class, "V");

        AUG_ELEMENT_TYPE_TO_LABEL.put(ContainsEdge.class, "Con");
        AUG_ELEMENT_TYPE_TO_LABEL.put(ExceptionHandlingEdge.class, "H");
        AUG_ELEMENT_TYPE_TO_LABEL.put(FinallyEdge.class, "F");
        AUG_ELEMENT_TYPE_TO_LABEL.put(OrderEdge.class, "O");
        AUG_ELEMENT_TYPE_TO_LABEL.put(RepetitionEdge.class, "Re");
        AUG_ELEMENT_TYPE_TO_LABEL.put(SelectionEdge.class, "S");
        AUG_ELEMENT_TYPE_TO_LABEL.put(SynchronizationEdge.class, "Sy");
        AUG_ELEMENT_TYPE_TO_LABEL.put(ThrowEdge.class, "TE");

        AUG_ELEMENT_TYPE_TO_LABEL.put(DefinitionEdge.class, "D");
        AUG_ELEMENT_TYPE_TO_LABEL.put(ParameterEdge.class, "P");
        AUG_ELEMENT_TYPE_TO_LABEL.put(QualifierEdge.class, "Q");
        AUG_ELEMENT_TYPE_TO_LABEL.put(ReceiverEdge.class, "R");
    }

    public PersistenceAUGDotExporter() {
        super(PersistenceAUGDotExporter::getNodeType, PersistenceAUGDotExporter::getEdgeType, new NodeAttributeProvider(new AttributeProvider()), null);
    }

    private static String getNodeType(Node node) {
        return AUG_ELEMENT_TYPE_TO_LABEL.get(node.getClass());
    }

    private static String getEdgeType(Edge edge) {
        return AUG_ELEMENT_TYPE_TO_LABEL.get(edge.getClass());
    }

    private static class NodeAttributeProvider implements ComponentAttributeProvider<Node> {
        private final AttributeProvider provider;

        private NodeAttributeProvider(AttributeProvider provider) {
            this.provider = provider;
        }

        @Override
        public Map<String, String> getComponentAttributes(Node node) {
            return node.apply(provider);
        }
    }

    private static class AttributeProvider implements NodeVisitor<Map<String, String>> {
        @Override
        public Map<String, String> visit(ArrayAccessNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(DECLARING_TYPE, node.getDeclaringTypeName());
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;
        }

        @Override
        public Map<String, String> visit(ArrayAssignmentNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(DECLARING_TYPE, node.getDeclaringTypeName());
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;
        }

        @Override
        public Map<String, String> visit(ArrayCreationNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(DECLARING_TYPE, node.getDeclaringTypeName());
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;
        }

        @Override
        public Map<String, String> visit(AssignmentNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;
        }

        @Override
        public Map<String, String> visit(BreakNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;
        }

        @Override
        public Map<String, String> visit(CastNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(TARGET_TYPE, node.getTargetType());
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;

        }

        @Override
        public Map<String, String> visit(CatchNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(CATCH_TYPE, node.getExceptionType());
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;

        }

        @Override
        public Map<String, String> visit(ConstructorCallNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(DECLARING_TYPE, node.getDeclaringTypeName());
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;

        }

        @Override
        public Map<String, String> visit(ContinueNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;

        }

        @Override
        public Map<String, String> visit(InfixOperatorNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(OPERATOR, node.getOperator());
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;

        }

        @Override
        public Map<String, String> visit(MethodCallNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(DECLARING_TYPE, node.getDeclaringTypeName());
            attributes.put(METHOD_SIGNATURE, node.getMethodSignature());
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;
        }

        @Override
        public Map<String, String> visit(NullCheckNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;

        }

        @Override
        public Map<String, String> visit(ReturnNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;

        }

        @Override
        public Map<String, String> visit(SuperConstructorCallNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(DECLARING_TYPE, node.getDeclaringTypeName());
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;

        }

        @Override
        public Map<String, String> visit(ThrowNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;

        }

        @Override
        public Map<String, String> visit(TypeCheckNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(TARGET_TYPE, node.getTargetTypeName());
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;

        }

        @Override
        public Map<String, String> visit(UnaryOperatorNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(OPERATOR, node.getOperator());
            node.getSourceLineNumber().ifPresent(lineNumber -> attributes.put(SOURCE_LINE_NUMBER, String.valueOf(lineNumber)));
            return attributes;

        }

        @Override
        public Map<String, String> visit(AnonymousClassMethodNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(DATA_TYPE, node.getType());
            attributes.put(METHOD_SIGNATURE, node.getName());
            return attributes;

        }

        @Override
        public Map<String, String> visit(AnonymousObjectNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(DATA_TYPE, node.getType());
            return attributes;

        }

        @Override
        public Map<String, String> visit(ConstantNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(DATA_TYPE, node.getType());
            attributes.put(DATA_NAME, node.getName());
            attributes.put(DATA_VALUE, node.getValue());
            return attributes;

        }

        @Override
        public Map<String, String> visit(ExceptionNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(DATA_TYPE, node.getType());
            attributes.put(DATA_NAME, node.getName());
            return attributes;

        }

        @Override
        public Map<String, String> visit(LiteralNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(DATA_TYPE, node.getType());
            attributes.put(DATA_VALUE, node.getValue());
            return attributes;
        }

        @Override
        public Map<String, String> visit(VariableNode node) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(DATA_TYPE, node.getType());
            attributes.put(DATA_NAME, node.getName());
            return attributes;
        }
    }
}
