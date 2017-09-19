package de.tu_darmstadt.stg.mudetect.aug;

import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.CONDITION;

public class ConditionEdge extends BaseEdge implements ControlFlowEdge {
    private final ConditionType conditionType;

    public enum ConditionType {
        SELECTION("sel"), REPETITION("rep");

        private final String label;

        ConditionType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public ConditionEdge(Node source, Node target, ConditionType conditionType) {
        super(source, target, CONDITION);
        this.conditionType = conditionType;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    @Override
    public String getLabel() {
        return getConditionType().getLabel();
    }
}
