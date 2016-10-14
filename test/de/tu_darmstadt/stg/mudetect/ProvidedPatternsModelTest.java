package de.tu_darmstadt.stg.mudetect;

import egroum.EGroumGraph;
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
                "}", null);

        Model model = new ProvidedPatternsModel(groums);

        assertThat(model.getPatterns(), hasSize(2));
    }
}
