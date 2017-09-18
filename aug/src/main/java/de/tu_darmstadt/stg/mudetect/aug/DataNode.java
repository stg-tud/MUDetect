package de.tu_darmstadt.stg.mudetect.aug;

public interface DataNode extends Node {
    @Override
    default boolean isCoreAction() {
        return false;
    }

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
}
