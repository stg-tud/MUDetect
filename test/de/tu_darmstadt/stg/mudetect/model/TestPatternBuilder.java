package de.tu_darmstadt.stg.mudetect.model;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;

public class TestPatternBuilder {
    public static Pattern somePattern() {
        return somePattern(someAUG());
    }

    public static Pattern somePattern(AUG patternAUG) {
        return new Pattern(patternAUG, 1);
    }
}
