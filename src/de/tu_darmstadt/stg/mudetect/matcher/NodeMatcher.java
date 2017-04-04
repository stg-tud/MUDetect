package de.tu_darmstadt.stg.mudetect.matcher;

import egroum.EGroumNode;

import java.util.function.BiPredicate;

public interface NodeMatcher extends BiPredicate<EGroumNode, EGroumNode> {}
