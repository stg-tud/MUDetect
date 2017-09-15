package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Overlap;

import java.util.Optional;
import java.util.function.Function;

public interface ViolationPredicate extends Function<Overlap, Optional<Boolean>> {}
