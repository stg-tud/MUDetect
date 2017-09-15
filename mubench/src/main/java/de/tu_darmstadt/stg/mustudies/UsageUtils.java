package de.tu_darmstadt.stg.mustudies;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.tu_darmstadt.stg.mudetect.model.AUG;
import egroum.EGroumGraph;

import java.util.Collection;

public class UsageUtils {
    public static Multiset<String> countNumberOfUsagesPerType(Collection usageGraphs) {
        HashMultiset<String> numberOfUsagesPerType = HashMultiset.create();
        for (Object usageGraph : usageGraphs) {
            if (usageGraph instanceof AUG) {
                numberOfUsagesPerType.addAll(((AUG) usageGraph).getAPIs());
            } else if (usageGraph instanceof EGroumGraph) {
                numberOfUsagesPerType.addAll(((EGroumGraph) usageGraph).getAPIs());
            } else {
                throw new IllegalArgumentException("Unknown type of usage graph: " + usageGraph.getClass().getSimpleName());
            }
        }
        return numberOfUsagesPerType;
    }
}
