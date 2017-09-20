package de.tu_darmstadt.stg.mudetect.aug.model.actions;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.BaseNode;

public class ArrayAccessNode extends MethodCallNode {
    public ArrayAccessNode(String declaringTypeAndMethodSignature) {
        super(declaringTypeAndMethodSignature);
    }

    @Override
    public boolean isCoreAction() {
        return false;
    }
}
