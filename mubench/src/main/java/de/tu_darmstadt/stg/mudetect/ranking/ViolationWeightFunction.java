package de.tu_darmstadt.stg.mudetect.ranking;

import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public interface ViolationWeightFunction {
    double getWeight(Overlap violation, Overlaps overlaps, Model model);

    String getFormula(Overlap violation, Overlaps overlaps, Model model);

    String getId();
}
