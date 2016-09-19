package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Pattern;

import java.util.Set;

public interface Model {
    Set<Pattern> getPatterns();
}
