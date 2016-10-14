package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.Model;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public class AverageWeightFunction implements ViolationWeightFunction {
    private final ViolationWeightFunction[] strategies;

    public AverageWeightFunction(ViolationWeightFunction... functions) {
        this.strategies = functions;
    }

    @Override
    public float getWeight(Instance violation, Overlaps overlaps, Model model) {
        float weight = 0;
        for (ViolationWeightFunction strategy : strategies) {
            weight += strategy.getWeight(violation, overlaps, model);
        }
        return weight / strategies.length;
    }
}
