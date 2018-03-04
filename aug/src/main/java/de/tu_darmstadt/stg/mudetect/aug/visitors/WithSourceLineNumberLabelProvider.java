package de.tu_darmstadt.stg.mudetect.aug.visitors;

import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.*;
import de.tu_darmstadt.stg.mudetect.aug.model.data.*;

public class WithSourceLineNumberLabelProvider extends DelegateAUGVisitor<String> implements AUGLabelProvider {
    public WithSourceLineNumberLabelProvider(AUGLabelProvider delegate) {
        super(delegate);
    }

    private String getLabel(BaseNode node, String baseLabel) {
        StringBuilder label = new StringBuilder(baseLabel);
        node.getSourceLineNumber().ifPresent(lineNumber -> label.append(" L").append(lineNumber));
        return label.toString();
    }

    @Override
    public String visit(ArrayAccessNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(ArrayAssignmentNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(ArrayCreationNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(AssignmentNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(BreakNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(CastNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(CatchNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(ConstructorCallNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(ContinueNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(InfixOperatorNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(MethodCallNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(NullCheckNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(ReturnNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(SuperConstructorCallNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(ThrowNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(TypeCheckNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(UnaryOperatorNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(AnonymousClassMethodNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(AnonymousObjectNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(ConstantNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(ExceptionNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(LiteralNode node) {
        return getLabel(node, super.visit(node));
    }

    @Override
    public String visit(VariableNode node) {
        return getLabel(node, super.visit(node));
    }
}
