package de.tu_darmstadt.stg.mudetect.model;

import egroum.EGroumDataEdge;
import egroum.EGroumEdge;
import egroum.EGroumNode;

import java.util.*;
import java.util.stream.Collectors;

import static egroum.EGroumDataEdge.Type.PARAMETER;

public class Equation {
    private static final String parameterEdgeLabel = EGroumDataEdge.getLabel(PARAMETER);

    private final String operator;
    private final Set<String> operands;

    Equation(EGroumNode operator, EGroumNode... operands) {
        this.operator = operator.getLabel();
        this.operands = getOperandLabels(Arrays.asList(operands));
    }

    Equation(EGroumNode operator, Set<EGroumNode> operands) {
        this.operator = operator.getLabel();
        this.operands = getOperandLabels(operands);
    }

    private Set<String> getOperandLabels(Collection<EGroumNode> operands) {
        return operands.stream().map(EGroumNode::getLabel).collect(Collectors.toSet());
    }

    public boolean isInstanceOf(Equation patternEquation) {
        return !operands.isEmpty() && equals(patternEquation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equation equation = (Equation) o;
        return Objects.equals(operator, equation.operator) &&
                Objects.equals(operands, equation.operands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, operands);
    }

    public static Equation from(EGroumNode operatorNode, AUG aug) {
        Map<String, Set<EGroumEdge>> inEdgesByType = aug.getInEdgesByType(operatorNode);
        if (!inEdgesByType.containsKey(parameterEdgeLabel)) {
            return new Equation(operatorNode);
        } else {
            Set<EGroumEdge> operands = inEdgesByType.get(parameterEdgeLabel);
            return new Equation(operatorNode, operands.stream().map(EGroumEdge::getSource).collect(Collectors.toSet()));
        }
    }
}
