package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasDefinitionEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.dataNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeDefinitionsTest {
    @Test
    public void encodesOnlyDirectDefinitionEdges() {
        APIUsageExample aug = buildAUG("void m(java.io.File f) {\n" +
                "  java.io.InputStream is = new java.io.FileInputStream(f);\n" +
                "  java.io.Reader r = new InputStreamReader(is);\n" +
                "  r.read();\n" +
                "}");

        assertThat(aug, hasDefinitionEdge(actionNodeWith(label("FileInputStream.<init>")), dataNodeWith(label("InputStream"))));
        assertThat(aug, not(hasDefinitionEdge(actionNodeWith(label("FileInputStream.<init>")), dataNodeWith(label("Reader")))));
    }

    @Test
    public void encodesTransitiveDefinitionEdgesThroughArithmeticOperator() {
    	AUGConfiguration conf = new AUGConfiguration(){{}};
        APIUsageExample aug = buildAUG("double m() {\n" +
        		"  java.util.List c = new ArrayList();" +
                "  double d = c.size() + 1f;\n" +
                "  return d;\n" +
                "}", conf);

        assertThat(aug, hasDefinitionEdge(actionNodeWith(label("ArrayList.<init>")), dataNodeWith(label("List"))));
        if (conf.buildTransitiveDataEdges)
            assertThat(aug, hasDefinitionEdge(actionNodeWith(label("Collection.size()")), dataNodeWith(label("double"))));
        if (conf.buildTransitiveDataEdges)
            assertThat(aug, not(hasDefinitionEdge(actionNodeWith(label("ArrayList.<init>")), dataNodeWith(label("double")))));
    }
}
