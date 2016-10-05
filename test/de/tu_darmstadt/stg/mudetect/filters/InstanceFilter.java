package de.tu_darmstadt.stg.mudetect.filters;

import de.tu_darmstadt.stg.mudetect.Instance;
import de.tu_darmstadt.stg.mudetect.model.Instances;

import java.util.function.BiPredicate;

public interface InstanceFilter extends BiPredicate<Instance, Instances> {}
