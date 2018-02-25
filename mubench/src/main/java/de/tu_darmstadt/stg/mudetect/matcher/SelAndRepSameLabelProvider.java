package de.tu_darmstadt.stg.mudetect.matcher;

import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.RepetitionEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.SelectionEdge;
import de.tu_darmstadt.stg.mudetect.aug.visitors.AUGElementVisitor;
import de.tu_darmstadt.stg.mudetect.aug.visitors.AUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.aug.visitors.DelegateAUGVisitor;

public class SelAndRepSameLabelProvider extends DelegateAUGVisitor<String> implements AUGLabelProvider {
    private static final String SEL_AND_REP_LABEL = "ctrl";

    public SelAndRepSameLabelProvider(AUGElementVisitor<String> fallbackLabelProvider) {
        super(fallbackLabelProvider);
    }

    @Override
    public String visit(SelectionEdge edge) {
        return SEL_AND_REP_LABEL;
    }

    @Override
    public String visit(RepetitionEdge edge) {
        return SEL_AND_REP_LABEL;
    }
}
