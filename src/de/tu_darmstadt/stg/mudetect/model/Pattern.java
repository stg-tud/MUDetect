package de.tu_darmstadt.stg.mudetect.model;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import egroum.EGroumNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Pattern extends AUG {
    private int support;
    private final Map<EGroumNode, Multiset<String>> literals;

    public Pattern(int support) {
        super("pattern", "model");
        this.support = support;
        this.literals = new HashMap<>();
    }

    public int getSupport() {
        return support;
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

    public void addLiteral(EGroumNode node, String literal) {
        if (!literals.containsKey(node)) {
            literals.put(node, HashMultiset.create());
        }
        literals.get(node).add(literal);
    }

    public Multiset<String> getLiterals(EGroumNode node) {
        return literals.getOrDefault(node, HashMultiset.create());
    }
}
