package edu.iastate.cs.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.AUGTestUtils;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUGsForClasses;
import static edu.iastate.cs.mudetect.mining.MinerTestUtils.mineWithMinSupport2;

public class DebugMiner {
    @Test
    public void debug() {
        String code = "class C {\n" +
                "    void m(java.io.InputStream is) {\n" +
                "        try {\n" +
                "            is.read();\n" +
                "        } catch (IOException e) {\n" +
                "            // ignore\n" +
                "        }\n" +
                "    }\n" +
                "}";

        ArrayList<APIUsageExample> augs = buildAUGsForClasses(new String[]{code, code});
        List<APIUsagePattern> patterns = mineWithMinSupport2(augs);

        AUGTestUtils.exportAUGsAsPNG(patterns, "./output", "debug-patterns");
    }
}
