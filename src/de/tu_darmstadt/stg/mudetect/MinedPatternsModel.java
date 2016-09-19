package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Pattern;
import egroum.EGroumGraph;
import mining.AUGMiner;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class MinedPatternsModel implements Model {
    private Set<Pattern> patterns;

    public MinedPatternsModel(int minPatternSupport, int minPatternSize, Collection<EGroumGraph> groums) {
        patterns = new AUGMiner(minPatternSupport, minPatternSize).mine(groums).stream()
                .map(Pattern::new).collect(Collectors.toSet());
    }

    @Override
    public Set<Pattern> getPatterns() {
        return patterns;
    }
}
