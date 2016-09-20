package de.tu_darmstadt.stg.mudetect.model;

import egroum.EGroumNode;

public class Equation {
    private final EGroumNode operand1;
    private final EGroumNode operator;
    private final EGroumNode operand2;

    public Equation(EGroumNode operand1, EGroumNode operator, EGroumNode operand2) {
        if (operand1 == null) {
            throw new IllegalArgumentException("operand1 must not be null");
        }
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null");
        }
        if (operand2 == null) {
            throw new IllegalArgumentException("operand2 must not be null");
        }
        this.operand1 = operand1;
        this.operator = operator;
        this.operand2 = operand2;
    }

    public boolean isInstanceOf(Equation patternEquation) {
        return (operand1.getLabel().equals(patternEquation.operand1.getLabel()))
                && (operator.getLabel().equals(patternEquation.operator.getLabel()))
                && (operand2.getLabel().equals(patternEquation.operand2.getLabel()));
    }
}
