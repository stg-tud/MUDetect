package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;

public class CatchNode extends BaseNode implements ActionNode {
    private final String exceptionType;

    public CatchNode(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public CatchNode(String exceptionType, int sourceLineNumber) {
        super(sourceLineNumber);
        this.exceptionType = exceptionType;
    }

    @Override
    public String getLabel() {
        return "<catch>";
    }

    public String getExceptionType() {
        return exceptionType;
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
