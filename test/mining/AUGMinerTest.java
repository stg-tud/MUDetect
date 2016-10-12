package mining;

import egroum.EGroumGraph;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static de.tu_darmstadt.stg.mudetect.model.PatternTestUtils.isPattern;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static egroum.EGroumTestUtils.buildGroumsForClass;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class AUGMinerTest {
    @Test
    public void findsPattern() throws Exception {
        List<EGroumGraph> groums = buildGroumsForClass("class A {" +
                "  void m(C c) { c.foo(); }" +
                "  void n(C c) { c.foo(); }" +
                "}");

        Set<de.tu_darmstadt.stg.mudetect.model.Pattern> patterns = new AUGMiner(2, 1).mine(groums);

        assertThat(patterns, contains(isPattern(buildAUG().withActionNode("C.foo()"), 2)));
    }
}
