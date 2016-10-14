package de.tu_darmstadt.stg.mudetect.model;

public class TestViolationBuilder {
    public static Violation someViolation(TestInstanceBuilder instance) {
        return someViolation(instance.build());
    }

    public static Violation someViolation(Instance violation) {
        return new Violation(violation, 1f);
    }
}
