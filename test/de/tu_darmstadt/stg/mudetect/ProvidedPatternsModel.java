package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Pattern;
import egroum.EGroumBuilder;
import egroum.EGroumGraph;
import mining.AUGMiner;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ProvidedPatternsModel implements Model {
    private Set<Pattern> patterns = new HashSet<>();

    public ProvidedPatternsModel(Collection<EGroumGraph> groums) {
        patterns = groums.stream()
                .flatMap(this::getMiningResultEquivalent)
                .map(Pattern::new)
                .collect(Collectors.toSet());
    }

    /**
     * Returns an {@link AUG} that represents how the given graph would look like. if it were returned by a call to
     * mine, instead of directly from the {@link EGroumBuilder}.
     */
    private Stream<AUG> getMiningResultEquivalent(EGroumGraph graph) {
        return new AUGMiner(1, 1).mine(graph).stream();
    }

    @Override
    public Set<Pattern> getPatterns() {
        return patterns;
    }
}
