package de.tu_darmstadt.stg.mudetect.ranking;

import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ProductWeightFunction implements ViolationWeightFunction {
    private final ViolationWeightFunction[] factors;

    public ProductWeightFunction(ViolationWeightFunction... factors) {
        this.factors = factors;
    }

    @Override
    public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
        return Arrays.stream(factors)
                .mapToDouble(factor -> factor.getWeight(violation, overlaps, model))
                .reduce(1, (a, b) -> a * b);
    }

    @Override
    public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
        return Arrays.stream(factors)
                .map(factor -> factor.getFormula(violation, overlaps, model))
                .collect(Collectors.joining(")*(", "(", ")"));
    }

    @Override
    public String getId() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < factors.length; i++) {
            if (i > 0) {
                sb.append("*");
            }
            sb.append(factors[i].getId());
        }
        return sb.toString();
    }
}
