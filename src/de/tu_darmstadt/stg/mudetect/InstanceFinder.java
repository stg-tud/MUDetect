package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;

import java.util.List;

public interface InstanceFinder {
    List<Instance> findInstances(AUG target, AUG pattern);
}
