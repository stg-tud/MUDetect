package de.tu_darmstadt.stg.mudetect.model;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;

public class TestPatternBuilder {
    public static Pattern somePattern() {
        return new Pattern(someAUG(), 1);
    }
}
