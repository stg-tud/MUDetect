package egroum;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasNode;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

public class EncodeClosureTest {

    @Test
    public void encodeClosure() {
    	AUGConfiguration conf = new AUGConfiguration() {{removeImplementationCode = 2;}};
        APIUsageExample aug = buildAUG(
                "void m(final String s) { new C(s.trim()) { void n() { s.length(); } }; }",
                conf);
        assertThat(aug, not(hasNode(actionNodeWithLabel("CharSequence.length()"))));
        assertThat(aug, hasNode(actionNodeWithLabel("String.trim()")));
    }
}
