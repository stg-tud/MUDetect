package de.tu_darmstadt.stg.mudetect.mining;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Location;
import egroum.EGroumNode;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class Pattern extends AUG {
    private final int support;
    private final Map<EGroumNode, Multiset<String>> literals;
    private final Set<Location> exampleLocations;

    private List<EGroumNode> meaningfulActionNodesByUniquenesCache = null;

    public Pattern(int support) {
        super("pattern", "model");
        this.support = support;
        this.literals = new HashMap<>();
        this.exampleLocations = new HashSet<>();
    }

    public int getSupport() {
        return support;
    }

    public List<EGroumNode> getMeaningfulActionNodesByUniqueness() {
        if (meaningfulActionNodesByUniquenesCache == null) {
            Set<EGroumNode> meaningfulActionNodes = getMeaningfulActionNodes();
            Map<String, List<EGroumNode>> actionNodesByLabel = meaningfulActionNodes.stream()
                    .collect(Collectors.groupingBy(EGroumNode::getLabel));
            meaningfulActionNodesByUniquenesCache = meaningfulActionNodes.stream()
                    .sorted(Comparator.comparing(node -> actionNodesByLabel.get(node.getLabel()).size()))
                    .collect(Collectors.toList());
        }
        return meaningfulActionNodesByUniquenesCache;
    }

    public void addExampleLocation(Location location) {
        exampleLocations.add(location);
    }

    public Set<Location> getExampleLocations() {
        return exampleLocations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Pattern pattern = (Pattern) o;
        return support == pattern.support &&
                Objects.equals(literals, pattern.literals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), support, literals);
    }

    @Override
    public String toString() {
        return "Pattern{" +
                "aug=" + super.toString() +
                ", support=" + support +
                '}';
    }

    void addLiteral(EGroumNode node, String literal) {
        if (!literals.containsKey(node)) {
            literals.put(node, HashMultiset.create());
        }
        literals.get(node).add(literal);
    }

    Multiset<String> getLiterals(EGroumNode node) {
        return literals.getOrDefault(node, HashMultiset.create());
    }
}
