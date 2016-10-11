package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;

import java.util.List;

public interface InstanceFinder {
    List<Instance> findInstances(AUG target, Pattern pattern);
}
