package de.tu_darmstadt.stg.mudetect.mining;

import egroum.EGroumGraph;
import mining.Configuration;
import org.junit.Test;

import java.util.List;

import static egroum.EGroumTestUtils.buildGroumsForClass;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class ProvidedPatternsModelTest {
    @Test
    public void convertsGroumToPatterns() throws Exception {
        List<EGroumGraph> groums = buildGroumsForClass("class A {" +
                "  void m(C c) { c.foo(); }" +
                "  void n(C c) { c.bar(); }" +
                "}");

        Model model = new ProvidedPatternsModel(new Configuration() {{ outputPath = null; }}, groums);

        assertThat(model.getPatterns(), hasSize(2));
    }
}
