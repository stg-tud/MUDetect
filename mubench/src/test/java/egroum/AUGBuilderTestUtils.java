package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;

import static egroum.EGroumTestUtils.buildGroumForMethod;

public class AUGBuilderTestUtils {
    public static AUG buildAUG(String code) {
        return AUGBuilder.toAUG(buildGroumForMethod(code));
    }

    static AUG buildAUG(String code, AUGConfiguration configuration) {
        return AUGBuilder.toAUG(buildGroumForMethod(code, configuration));
    }
}
