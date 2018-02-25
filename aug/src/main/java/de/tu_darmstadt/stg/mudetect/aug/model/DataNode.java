package de.tu_darmstadt.stg.mudetect.aug.model;

import java.util.Optional;

public interface DataNode extends Node {
    /**
     * @return the data's symbolic name, i.e., variable or field name
     */
    String getName();

    /**
     * @return the data's value, if statically known, e.g., for literals or constants
     */
    String getValue();

    /**
     * @return the data's type name
     */
    String getType();

    @Override
    default Optional<String> getAPI() {
        String dataType = getType();
        switch (dataType) {
            case "int":
            case "long":
            case "float":
            case "double":
            case "short":
            case "boolean":
            case "null":
                return Optional.empty();
            default:
                if (dataType.endsWith("[]")) {
                    return Optional.empty();
                } else {
                    return Optional.of(getType());
                }
        }
    }
}
