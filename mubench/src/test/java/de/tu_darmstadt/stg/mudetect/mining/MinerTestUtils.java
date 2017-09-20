package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;

import java.util.*;

import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUGsForMethods;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

class MinerTestUtils {
    private static List<APIUsagePattern> mine(Collection<APIUsageExample> examples, Configuration config) {
        AUGMiner miner = new DefaultAUGMiner(config);
        return new ArrayList<>(miner.mine(examples).getPatterns());
    }

    static List<APIUsagePattern> mineMethods(Configuration config, String... sourceCodes) {
        return mine(buildGroumsForMethods(sourceCodes), config);
    }

    static List<APIUsagePattern> mineWithMinSupport(Collection<APIUsageExample> examples, int minSupport) {
        Configuration config = new Configuration() {{
            minPatternSupport = minSupport;
            maxPatternSize = 300;
        }};
        return mine(examples, config);
    }

    static List<APIUsagePattern> mineWithMinSupport2(Collection<APIUsageExample> examples) {
        return mineWithMinSupport(examples, 2);
    }

    static List<APIUsagePattern> mineMethodsWithMinSupport2(String... sourceCodes) {
        return mineWithMinSupport2(buildAUGsForMethods(sourceCodes));
    }

    static void print(APIUsagePattern pattern) {
        System.out.println(new DisplayAUGDotExporter().toDotGraph(pattern));
    }

    static void print(List<APIUsagePattern> patterns) {
        for (APIUsagePattern pattern : patterns) {
            print(pattern);
        }
    }
}
