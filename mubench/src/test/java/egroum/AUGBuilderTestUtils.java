package egroum;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;

import static egroum.EGroumTestUtils.buildGroumForMethod;

public class AUGBuilderTestUtils {
    public static APIUsageExample buildAUG(String code) {
        return buildGroumForMethod(code);
    }

    static APIUsageExample buildAUG(String code, AUGConfiguration configuration) {
        return buildGroumForMethod(code, configuration);
    }
}
