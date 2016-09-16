package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.Instance;

public class Violation {

    private Instance instance;

    public Violation(Instance overlap) {
        this.instance = overlap;
    }

    public Instance getInstance() {
        return instance;
    }
}
