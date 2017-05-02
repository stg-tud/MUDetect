package de.tu_darmstadt.stg.mustudies;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.tu_darmstadt.stg.mudetect.model.AUG;

import java.util.Collection;

public class UsageUtils {
    public static Multiset<String> countNumberOfUsagesPerType(Collection<AUG> augs) {
        HashMultiset<String> numberOfUsagesPerType = HashMultiset.create();
        for (AUG aug : augs) {
            numberOfUsagesPerType.addAll(aug.getAPIs());
        }
        return numberOfUsagesPerType;
    }
}
