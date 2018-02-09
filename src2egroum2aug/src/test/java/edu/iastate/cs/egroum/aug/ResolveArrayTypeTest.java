package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.dataNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;

public class ResolveArrayTypeTest {

    @Test
    public void resolveArrayTypeTest1() {;
    	AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG(
                "void m(String s[]) { if (s.length > 0) s[0].getBytes(); }",
                conf);
        MatcherAssert.assertThat(aug, hasNode(dataNodeWith(label("String[]"))));
    }

    @Test
    public void resolveArrayTypeTest2() {;
    	AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG(
                "void m() { String s[]; if (s.length > 0) s[0].getBytes(); }",
                conf);
        MatcherAssert.assertThat(aug, hasNode(dataNodeWith(label("String[]"))));
    }

    @Test
    public void resolveArrayTypeTest3() {;
    	AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG(
                "void m() { for (String s[] = {}; ;) s[0].getBytes(); }",
                conf);
        MatcherAssert.assertThat(aug, hasNode(dataNodeWith(label("String[]"))));
    }
}
