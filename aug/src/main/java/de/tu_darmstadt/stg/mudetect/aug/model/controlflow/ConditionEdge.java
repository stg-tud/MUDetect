package de.tu_darmstadt.stg.mudetect.aug.model.controlflow;

import de.tu_darmstadt.stg.mudetect.aug.model.BaseEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.ControlFlowEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.CONDITION;

public abstract class ConditionEdge extends BaseEdge implements ControlFlowEdge {
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

    protected ConditionEdge(Node source, Node target, ConditionType conditionType) {
        super(source, target, CONDITION);
        this.conditionType = conditionType;
    }

    /**
     * Use the edge's class type instead.
     */
    @Deprecated
    public ConditionType getConditionType() {
        return conditionType;
    }
}
