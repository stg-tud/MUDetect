package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGNodeMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.dataNodeWithLabel;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;

public class ResolveArrayTypeTest {

    @Test
    public void resolveArrayTypeTest1() {;
    	AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG(
                "void m(String s[]) { if (s.length > 0) s[0].getBytes(); }",
                conf);
        MatcherAssert.assertThat(aug, hasNode(dataNodeWithLabel("String[]")));
    }

    @Test
    public void resolveArrayTypeTest2() {;
    	AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG(
                "void m() { String s[]; if (s.length > 0) s[0].getBytes(); }",
                conf);
        MatcherAssert.assertThat(aug, hasNode(dataNodeWithLabel("String[]")));
    }

    @Test
    public void resolveArrayTypeTest3() {;
    	AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG(
                "void m() { for (String s[] = {}; ;) s[0].getBytes(); }",
                conf);
        MatcherAssert.assertThat(aug, hasNode(dataNodeWithLabel("String[]")));
    }
}
