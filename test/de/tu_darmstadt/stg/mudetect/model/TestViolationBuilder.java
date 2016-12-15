package de.tu_darmstadt.stg.mudetect.model;

public class TestViolationBuilder {
    public static Violation someViolation(TestOverlapBuilder overlap) {
        return someViolation(overlap.build());
    }

    public static Violation someViolation(Overlap violation) {
        return new Violation(violation, 1f, "constant rank");
    }
}
