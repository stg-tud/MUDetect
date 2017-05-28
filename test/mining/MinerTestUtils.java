package mining;

import egroum.EGroumGraph;
import egroum.EGroumNode;
import org.junit.rules.TestName;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static egroum.EGroumTestUtils.buildGroumsForMethods;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

class MinerTestUtils {
    static List<Pattern> mineWithMinSupport2(ArrayList<EGroumGraph> groums) {
        Miner miner = new Miner("test", new Configuration() {{ minPatternSupport = 2; maxPatternSize = 300; }});
        return new ArrayList<>(miner.mine(groums));
    }

    static List<Pattern> mineMethodsWithMinSupport2(String... sourceCodes) {
        return mineWithMinSupport2(buildGroumsForMethods(sourceCodes));
    }

    static void print(List<Pattern> patterns, TestName testName) {
        System.err.println("Test '" + testName.getMethodName() + "':");
        for (Pattern pattern : patterns) {
            HashSet<EGroumNode> set = new HashSet<>(pattern.getRepresentative().getNodes());
            assertThat(set.size(), is(pattern.getRepresentative().getNodes().size()));
            EGroumGraph g = new EGroumGraph(pattern.getRepresentative());
            System.err.println(" - Support=" + pattern.getFreq());
            System.err.println(" - Patter=" + g);
            File dir = new File("temp/" + pattern.getId() + "/");
            dir.mkdirs();
            g.toGraphics(dir.getAbsolutePath());
        }
    }

    static void print(Pattern pattern, TestName testName) {
        print(Collections.singletonList(pattern), testName);
    }
}
