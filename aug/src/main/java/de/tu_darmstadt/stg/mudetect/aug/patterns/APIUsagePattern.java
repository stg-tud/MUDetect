package de.tu_darmstadt.stg.mudetect.aug.patterns;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.Location;
import de.tu_darmstadt.stg.mudetect.aug.Node;

import java.util.*;
import java.util.stream.Collectors;

public class APIUsagePattern extends APIUsageGraph {
    private final int support;
    private final Set<Location> exampleLocations;

    private List<Node> meaningfulActionNodesByUniquenesCache = null;

    public APIUsagePattern(int support, Set<Location> exampleLocations) {
        this.support = support;
        this.exampleLocations = exampleLocations;
    }

    public int getSupport() {
        return support;
    }

    public Set<Location> getExampleLocations() {
        return exampleLocations;
    }

    public List<Node> getMeaningfulActionNodesByUniqueness() {
        if (meaningfulActionNodesByUniquenesCache == null) {
            Set<Node> meaningfulActionNodes = getMeaningfulActionNodes();
            Map<String, List<Node>> actionNodesByLabel = meaningfulActionNodes.stream()
                    .collect(Collectors.groupingBy(Node::getLabel));
            meaningfulActionNodesByUniquenesCache = meaningfulActionNodes.stream()
                    .sorted(Comparator.comparing(node -> actionNodesByLabel.get(node.getLabel()).size()))
                    .collect(Collectors.toList());
        }
        return meaningfulActionNodesByUniquenesCache;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        APIUsagePattern pattern = (APIUsagePattern) o;
        return support == pattern.support;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), support);
    }
}
