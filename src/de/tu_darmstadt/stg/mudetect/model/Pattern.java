package de.tu_darmstadt.stg.mudetect.model;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import egroum.EGroumNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Pattern {
    private final AUG aug;
    private int support;
    private final Map<EGroumNode, Multiset<String>> literals;

    public Pattern(AUG aug, int support) {
        this(aug, support, new HashMap<>());
    }
    public Pattern(AUG aug, int support, Map<EGroumNode, Multiset<String>> literals) {
        this.aug = aug;
        this.support = support;
        this.literals = literals;
    }

    public AUG getAUG() {
        return aug;
    }

    public int getSupport() {
        return support;
    }

    public int getNodeSize() {
        return aug.vertexSet().size();
    }

    public int getEdgeSize() {
        return aug.edgeSet().size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pattern pattern = (Pattern) o;
        return support == pattern.support &&
                Objects.equals(aug, pattern.aug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aug, support);
    }

    @Override
    public String toString() {
        return "Pattern{" +
                "aug=" + aug +
                ", support=" + support +
                '}';
    }

    public Multiset<String> getLiterals(EGroumNode node) {
        return literals.getOrDefault(node, HashMultiset.create());
    }
}
