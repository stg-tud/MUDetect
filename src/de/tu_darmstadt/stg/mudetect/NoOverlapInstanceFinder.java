package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;

import java.util.Collections;
import java.util.List;

public class NoOverlapInstanceFinder implements InstanceFinder {
    private InstanceFinder finder;

    public NoOverlapInstanceFinder(InstanceFinder finder) {
        this.finder = finder;
    }

    @Override
    public List<Instance> findInstances(AUG target, AUG pattern) {
        List<Instance> instances = finder.findInstances(target, pattern);
        if (instances.isEmpty()) {
            return Collections.singletonList(
                    new Instance(pattern, target, Collections.emptyMap(), Collections.emptyMap()));
        } else {
            return instances;
        }
    }
}
