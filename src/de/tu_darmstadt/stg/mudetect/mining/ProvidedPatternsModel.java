package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import egroum.EGroumBuilder;
import egroum.EGroumGraph;
import mining.Configuration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProvidedPatternsModel implements Model {
    private Set<Pattern> patterns = new HashSet<>();

    public ProvidedPatternsModel(Configuration config, Collection<EGroumGraph> groums) {
        config.minPatternSupport = 1;
        patterns = groums.stream()
                .flatMap((graph) -> getMiningResultEquivalent(graph, config))
                .collect(Collectors.toSet());
    }

    /**
     * Returns an {@link AUG} that represents how the given graph would look like, if it were returned by a call to
     * mine, instead of directly from the {@link EGroumBuilder}.
     */
    private Stream<Pattern> getMiningResultEquivalent(EGroumGraph graph, Configuration config) {
        return new AUGMiner(config).mine(graph).stream();
    }

    @Override
    public Set<Pattern> getPatterns() {
        return patterns;
    }
}
