package de.tu_darmstadt.stg.mudetect.aug;

public interface Edge {
    enum Type {
        RECEIVER, PARAMETER, DEFINITION, THROW, SYNCHRONIZE, CONDITION, ORDER, CONTAINS, FINALLY;
    }

    Node getSource();

    Node getTarget();

    Type getType();

    default boolean isDirect() {
        return true;
    }

    default String getLabel() {
        return getType().toString();
    }
}
