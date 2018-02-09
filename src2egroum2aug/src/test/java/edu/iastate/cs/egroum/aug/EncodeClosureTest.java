package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

public class EncodeClosureTest {

    @Test
    public void encodeClosure() {
    	AUGConfiguration conf = new AUGConfiguration() {{removeImplementationCode = 2;}};
        APIUsageExample aug = buildAUG(
                "void m(final String s) { new C(s.trim()) { void n() { s.length(); } }; }",
                conf);
        assertThat(aug, not(hasNode(actionNodeWith(label("CharSequence.length()")))));
        assertThat(aug, hasNode(actionNodeWith(label("String.trim()"))));
    }
}
