package de.tu_darmstadt.stg.mudetect.aug.model.dot;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;

import java.util.Map;

public interface AUGAttributeProvider<G extends APIUsageGraph> {
    Map<String, String> getAUGAttributes(G aug);
}
