package de.tu_darmstadt.stg.mudetect.aug;

import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.CONDITION;

public class ConditionEdge extends BaseEdge implements ControlFlowEdge {
    private final ConditionType conditionType;

    public enum ConditionType {
        SELECTION, REPETITION
    }

    public ConditionEdge(Node source, Node target, ConditionType conditionType) {
        super(source, target, CONDITION);
        this.conditionType = conditionType;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }
}
