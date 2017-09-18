package egroum;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResolveArrayTypeTest {

    @Test
    public void resolveArrayTypeTest1() {;
    	AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG(
                "void m(String s[]) { if (s.length > 0) s[0].getBytes(); }",
                conf);
        assertThat(aug, hasNode(dataNodeWithLabel("String[]")));
    }

    @Test
    public void resolveArrayTypeTest2() {;
    	AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG(
                "void m() { String s[]; if (s.length > 0) s[0].getBytes(); }",
                conf);
        assertThat(aug, hasNode(dataNodeWithLabel("String[]")));
    }

    @Test
    public void resolveArrayTypeTest3() {;
    	AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG(
                "void m() { for (String s[] = {}; ;) s[0].getBytes(); }",
                conf);
        assertThat(aug, hasNode(dataNodeWithLabel("String[]")));
    }
}
