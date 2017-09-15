package de.tu_darmstadt.stg.mudetect.ranking;

import egroum.EGroumNode;

public class ConstantNodeWeightFunction implements NodeWeightFunction {
    @Override
    public double getWeight(EGroumNode node) {
        return 1;
    }
}
