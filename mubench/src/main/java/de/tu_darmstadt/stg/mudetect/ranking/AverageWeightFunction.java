package de.tu_darmstadt.stg.mudetect.ranking;

import edu.iastate.cs.mudetect.mining.Model;
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

    @Override
    public String getId() {
        StringBuilder sb = new StringBuilder("AVG(");
        for (int i = 0; i < strategies.length; i++) {
            if (i > 0) {
                sb.append("+");
            }
            sb.append(strategies[i].getId());
        }
        return sb.append(")").toString();
    }
}
