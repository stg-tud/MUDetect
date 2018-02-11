package edu.iastate.cs.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;

import java.util.Collection;

public interface AUGMiner {
    Model mine(Collection<APIUsageExample> examples);
}
