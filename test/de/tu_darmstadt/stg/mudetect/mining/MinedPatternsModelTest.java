package de.tu_darmstadt.stg.mudetect.mining;

import egroum.EGroumGraph;
import mining.Configuration;
import org.junit.Test;

import java.util.List;

import static egroum.EGroumTestUtils.buildGroumsForClass;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class MinedPatternsModelTest {
    @Test
    public void findsPattern() throws Exception {
        List<EGroumGraph> groums = buildGroumsForClass("class A {" +
                "  void m(C c) { c.foo(); }" +
                "  void n(C c) { c.foo(); }" +
                "}");

        Model model = new MinedPatternsModel(new Configuration() {{ minPatternSupport = 2; }}, groums);

        assertThat(model.getPatterns(), hasSize(1));
    }
}
