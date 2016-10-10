package de.tu_darmstadt.stg.mudetect.model;

import java.util.*;
import java.util.function.Consumer;

public class Instances implements Iterable<Instance> {
    private final Set<Instance> instances;

    public Instances(Instance... instances) {
        this.instances = new HashSet<>(Arrays.asList(instances));
    }

    public void addAll(Collection<Instance> instances) {
        this.instances.addAll(instances);
    }

    @Override
    public Iterator<Instance> iterator() {
        return instances.iterator();
    }

    @Override
    public void forEach(Consumer<? super Instance> action) {
        instances.forEach(action);
    }

    @Override
    public Spliterator<Instance> spliterator() {
        return instances.spliterator();
    }
}
