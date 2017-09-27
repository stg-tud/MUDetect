package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.MatcherAssert.assertThat;

public class EncodeConstantTest {

    @Test
    public void encodeConstantTest() throws Exception {
    	AUGConfiguration conf = new AUGConfiguration();
        AUG aug = buildAUG(
                "void m(String s) { if (s.length() < Integer.MAX_VALUE) s.getBytes(); }",
                conf);
        assertThat(aug, hasNode(dataNodeWithName("2147483647")));
    }
}
