package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public class PatternSizeWeightFunction implements ViolationWeightFunction {
    @Override
    public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
        return Math.log10(getPatternWeight(violation)) / Math.log10(getMaxPatternWeight(model));
    }

    private int getPatternWeight(Overlap violation) {
        return violation.getPattern().getNodeSize();
    }

    private int getMaxPatternWeight(Model model) {
        return model.getPatterns().stream().mapToInt(APIUsageGraph::getNodeSize).max().orElse(Integer.MAX_VALUE);
    }

    @Override
    public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
        return String.format("pattern-size rank = log10(%d) / log10(%d)",
                getPatternWeight(violation),
                getMaxPatternWeight(model));
    }

    @Override
    public String getId() {
        return "PSize";
    }
}
