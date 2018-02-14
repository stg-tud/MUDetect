package de.tu_darmstadt.stg.mudetect.aug.model.patterns;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.model.Location;

import java.util.*;

public class APIUsagePattern extends APIUsageGraph {
    private final int support;
    private final Set<Location> exampleLocations;

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
