package de.tu_darmstadt.stg.mudetect.mining;

import egroum.EGroumGraph;
import mining.Configuration;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class MinedPatternsModel implements Model {
    private Set<Pattern> patterns;

    public MinedPatternsModel(Configuration config, Collection<EGroumGraph> groums) {
        patterns = new AUGMiner(config).mine(groums).stream().collect(Collectors.toSet());
    }

    @Override
    public Set<Pattern> getPatterns() {
        return patterns;
    }
}
