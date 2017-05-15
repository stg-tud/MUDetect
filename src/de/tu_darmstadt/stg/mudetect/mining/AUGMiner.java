package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.mining.Model;
import egroum.EGroumGraph;

import java.util.Collection;

public interface AUGMiner {
    Model mine(Collection<EGroumGraph> examples);
}
