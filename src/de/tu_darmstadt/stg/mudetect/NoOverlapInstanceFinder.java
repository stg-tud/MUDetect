package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Instance;
import de.tu_darmstadt.stg.mudetect.model.Pattern;

import java.util.Collections;
import java.util.List;

public class NoOverlapInstanceFinder implements InstanceFinder {
    private InstanceFinder finder;

    public NoOverlapInstanceFinder(InstanceFinder finder) {
        this.finder = finder;
    }

    @Override
    public List<Instance> findInstances(AUG target, Pattern pattern) {
        List<Instance> instances = finder.findInstances(target, pattern);
        if (instances.isEmpty()) {
            return Collections.singletonList(
                    new Instance(pattern, target, Collections.emptyMap(), Collections.emptyMap()));
        } else {
            return instances;
        }
    }
}
