package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import edu.iastate.cs.mudetect.mining.Model;

import java.util.List;

public interface ViolationRankingStrategy {
    List<Violation> rankViolations(Overlaps overlaps, Model model);
}
