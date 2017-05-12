package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mudetect.mining.Model;
import egroum.EGroumGraph;

import java.util.Collection;

public interface Miner {
    Model mine(Collection<EGroumGraph> examples);
}
