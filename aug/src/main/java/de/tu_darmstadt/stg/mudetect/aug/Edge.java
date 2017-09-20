package de.tu_darmstadt.stg.mudetect.aug;

public interface Edge {
    enum Type {
        RECEIVER("recv"),
        PARAMETER("para"),
        DEFINITION("def"),
        THROW("throw"),
        SYNCHRONIZE("syn"),
        CONDITION("cond"),
        ORDER("order"),
        CONTAINS("contains"),
        FINALLY("finally"),
        QUALIFIER("qual"),
        EXCEPTION_HANDLING("cond");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    Node getSource();

    Node getTarget();

    /**
     * Use the edge's class type instead.
     */
    @Deprecated
    Type getType();

    default boolean isDirect() {
        return true;
    }

    default String getLabel() {
        return getType().getLabel();
    }
}
