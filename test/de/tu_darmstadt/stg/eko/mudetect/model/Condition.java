package de.tu_darmstadt.stg.eko.mudetect.model;

import egroum.EGroumActionNode;
import egroum.EGroumNode;

import java.util.Objects;

public class Condition {
    private final EGroumNode operand1;
    private final EGroumActionNode operator;
    private final EGroumNode operand2;

    public Condition(EGroumNode operand1, EGroumActionNode operator, EGroumNode operand2) {
        if (operand1 == null) {
            throw new IllegalArgumentException("operand1 must not be null");
        }
        if (operator == null ^ operand2 == null) {
            throw new IllegalArgumentException("need either both or none of operator and operand2");
        }
        this.operand1 = operand1;
        this.operator = operator;
        this.operand2 = operand2;
    }

    public Condition(EGroumActionNode operand1) {
        this(operand1, null, null);
    }

    @Deprecated
    public EGroumActionNode getNode() {
        return operator != null ? operator : (EGroumActionNode) operand1;
    }

    public boolean isInstanceOf(Condition patternCondition) {
        if (operator == null) {
            return patternCondition.operator == null
                    && (operand1.getLabel().equals(patternCondition.operand1.getLabel()));

        } else {
            return patternCondition.operator != null
                    && (operand1.getLabel().equals(patternCondition.operand1.getLabel()))
                    && (operator.getLabel().equals(patternCondition.operator.getLabel()))
                    && (operand2.getLabel().equals(patternCondition.operand2.getLabel()));

        }
    }
}
