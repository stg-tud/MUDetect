package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public class AverageWeightFunction implements ViolationWeightFunction {
    private final ViolationWeightFunction[] strategies;

    public AverageWeightFunction(ViolationWeightFunction... functions) {
        this.strategies = functions;
    }

    @Override
    public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
        double weight = 0;
        for (ViolationWeightFunction strategy : strategies) {
            weight += strategy.getWeight(violation, overlaps, model);
        }
        return weight / strategies.length;
    }

    public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (ViolationWeightFunction strategy : strategies) {
            stringBuilder.append("(").append(strategy.getFormula(violation, overlaps, model)).append(")");
        }
        stringBuilder.append(") / ").append(strategies.length);
        return stringBuilder.toString();
    }
}
