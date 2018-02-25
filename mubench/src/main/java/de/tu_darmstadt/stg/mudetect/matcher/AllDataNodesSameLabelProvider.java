package de.tu_darmstadt.stg.mudetect.matcher;

import de.tu_darmstadt.stg.mudetect.aug.model.data.ConstantNode;
import de.tu_darmstadt.stg.mudetect.aug.model.data.ExceptionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.data.LiteralNode;
import de.tu_darmstadt.stg.mudetect.aug.model.data.VariableNode;
import de.tu_darmstadt.stg.mudetect.aug.visitors.AUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.aug.visitors.DelegateAUGVisitor;

public class AllDataNodesSameLabelProvider extends DelegateAUGVisitor<String> implements AUGLabelProvider {
    private static final String DATA_NODE_LABEL = "<Object>";

    public AllDataNodesSameLabelProvider(AUGLabelProvider fallbackLabelProvider) {
        super(fallbackLabelProvider);
    }

    @Override
    public String visit(LiteralNode node) {
        return DATA_NODE_LABEL;
    }

    @Override
    public String visit(ConstantNode node) {
        return DATA_NODE_LABEL;
    }

    @Override
    public String visit(VariableNode node) {
        return DATA_NODE_LABEL;
    }

    @Override
    public String visit(ExceptionNode node) {
        return DATA_NODE_LABEL;
    }
}
