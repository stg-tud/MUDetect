package de.tu_darmstadt.stg.mudetect.aug.persistence;

import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.*;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.*;
import de.tu_darmstadt.stg.mudetect.aug.model.data.*;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.DefinitionEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ParameterEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.QualifierEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ReceiverEdge;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTImporter;

import java.util.Map;

import static de.tu_darmstadt.stg.mudetect.aug.persistence.PersistenceAUGDotExporter.*;

public class PersistenceAUGDotImporter extends DOTImporter<Node, Edge> {
    private static final Map<String, Class> AUG_ELEMENT_LABEL_TO_TYPE = PersistenceAUGDotExporter.AUG_ELEMENT_TYPE_TO_LABEL.inverse();

    public PersistenceAUGDotImporter() {
        super(PersistenceAUGDotImporter::createNode, PersistenceAUGDotImporter::createEdge);
    }

    private static Node createNode(String label, Map<String, Attribute> attributes) {
        Class nodeType = AUG_ELEMENT_LABEL_TO_TYPE.get(label);

        if (nodeType == ArrayAccessNode.class) {
            return new ArrayAccessNode(get(attributes, DECLARING_TYPE), getSourceLineNumber(attributes));
        }
        if (nodeType == ArrayAssignmentNode.class) {
            return new ArrayAssignmentNode(get(attributes, DECLARING_TYPE), getSourceLineNumber(attributes));
        }
        if (nodeType == ArrayCreationNode.class) {
            return new ArrayCreationNode(get(attributes, DECLARING_TYPE), getSourceLineNumber(attributes));
        }
        if (nodeType == AssignmentNode.class) {
            return new AssignmentNode(getSourceLineNumber(attributes));
        }
        if (nodeType == BreakNode.class) {
            return new BreakNode(getSourceLineNumber(attributes));
        }
        if (nodeType == CastNode.class) {
            return new CastNode(get(attributes, TARGET_TYPE), getSourceLineNumber(attributes));
        }
        if (nodeType == CatchNode.class) {
            return new CatchNode(get(attributes, CATCH_TYPE), getSourceLineNumber(attributes));
        }
        if (nodeType == ConstructorCallNode.class) {
            return new ConstructorCallNode(get(attributes, DECLARING_TYPE), getSourceLineNumber(attributes));
        }
        if (nodeType == ContinueNode.class) {
            return new ContinueNode(getSourceLineNumber(attributes));
        }
        if (nodeType == InfixOperatorNode.class) {
            return new InfixOperatorNode(get(attributes, OPERATOR), getSourceLineNumber(attributes));
        }
        if (nodeType == MethodCallNode.class) {
            return new MethodCallNode(get(attributes, DECLARING_TYPE), get(attributes, METHOD_SIGNATURE), getSourceLineNumber(attributes));
        }
        if (nodeType == NullCheckNode.class) {
            return new NullCheckNode(getSourceLineNumber(attributes));
        }
        if (nodeType == ReturnNode.class) {
            return new ReturnNode(getSourceLineNumber(attributes));
        }
        if (nodeType == SuperConstructorCallNode.class) {
            return new SuperConstructorCallNode(get(attributes, DECLARING_TYPE), getSourceLineNumber(attributes));
        }
        if (nodeType == ThrowNode.class) {
            return new ThrowNode(getSourceLineNumber(attributes));
        }
        if (nodeType == TypeCheckNode.class) {
            return new TypeCheckNode(get(attributes, TARGET_TYPE), getSourceLineNumber(attributes));
        }
        if (nodeType == UnaryOperatorNode.class) {
            return new UnaryOperatorNode(get(attributes, OPERATOR), getSourceLineNumber(attributes));
        }

        if (nodeType == AnonymousClassMethodNode.class) {
            return new AnonymousClassMethodNode(get(attributes, DATA_TYPE), get(attributes, METHOD_SIGNATURE));
        }
        if (nodeType == AnonymousObjectNode.class) {
            return new AnonymousObjectNode(get(attributes, DATA_TYPE));
        }
        if (nodeType == ConstantNode.class) {
            return new ConstantNode(get(attributes, DATA_TYPE), get(attributes, DATA_NAME), get(attributes, DATA_VALUE));
        }
        if (nodeType == ExceptionNode.class) {
            return new ExceptionNode(get(attributes, DATA_TYPE), get(attributes, DATA_NAME));
        }
        if (nodeType == LiteralNode.class) {
            return new LiteralNode(get(attributes, DATA_TYPE), get(attributes, DATA_VALUE));
        }
        if (nodeType == VariableNode.class) {
            return new VariableNode(get(attributes, DATA_TYPE), get(attributes, DATA_NAME));
        }

        throw new IllegalArgumentException("unsupported node type: " + label);
    }

    private static String get(Map<String, Attribute> attributes, String declaringType) {
        return attributes.get(declaringType).getValue();
    }

    private static int getSourceLineNumber(Map<String, Attribute> attributes) {
        if (attributes.containsKey(SOURCE_LINE_NUMBER)) {
            return Integer.parseInt(get(attributes, SOURCE_LINE_NUMBER));
        } else {
            return -1;
        }
    }

    private static Edge createEdge(Node from, Node to, String label, Map<String, Attribute> attributes) {
        Class edgeType = AUG_ELEMENT_LABEL_TO_TYPE.get(label);

        if (edgeType == ContainsEdge.class) {
            return new ContainsEdge(from, to);
        }
        if (edgeType == ExceptionHandlingEdge.class) {
            return new ExceptionHandlingEdge(from, to);
        }
        if (edgeType == FinallyEdge.class) {
            return new FinallyEdge(from, to);
        }
        if (edgeType == OrderEdge.class) {
            return new OrderEdge(from, to);
        }
        if (edgeType == RepetitionEdge.class) {
            return new RepetitionEdge(from, to);
        }
        if (edgeType == SelectionEdge.class) {
            return new SelectionEdge(from, to);
        }
        if (edgeType == SynchronizationEdge.class) {
            return new SynchronizationEdge(from, to);
        }
        if (edgeType == ThrowEdge.class) {
            return new ThrowEdge(from, to);
        }

        if (edgeType == DefinitionEdge.class) {
            return new DefinitionEdge(from, to);
        }
        if (edgeType == ParameterEdge.class) {
            return new ParameterEdge(from, to);
        }
        if (edgeType == QualifierEdge.class) {
            return new QualifierEdge(from, to);
        }
        if (edgeType == ReceiverEdge.class) {
            return new ReceiverEdge(from, to);
        }

        throw new IllegalArgumentException("unsupported edge type: " + label);
    }
}
