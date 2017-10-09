package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static org.hamcrest.MatcherAssert.assertThat;

public class EncodeClosureTest {

    @Test
    public void encodeClosure() throws Exception {
    	AUGConfiguration conf = new AUGConfiguration() {{removeImplementationCode = 2;}};
        AUG aug = buildAUG(
                "void m(final String s) { new C(s.trim()) { void n() { s.length(); } }; }",
                conf);
        assertThat(aug, notHaveNode(actionNodeWithLabel("CharSequence.length()")));
        assertThat(aug, hasNode(actionNodeWithLabel("String.trim()")));
    }
}
