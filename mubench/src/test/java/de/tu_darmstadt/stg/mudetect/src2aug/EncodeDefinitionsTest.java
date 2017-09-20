package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.DEFINITION;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.dataNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasEdge;
import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeDefinitionsTest {
    @Test
    public void encodesOnlyDirectDefinitionEdges() throws Exception {
        APIUsageExample aug = AUGBuilderTestUtils.buildAUG("void m(java.io.File f) {\n" +
                "  java.io.InputStream is = new java.io.FileInputStream(f);\n" +
                "  java.io.Reader r = new InputStreamReader(is);\n" +
                "  r.read();\n" +
                "}");

        assertThat(aug, hasEdge(actionNodeWithLabel("FileInputStream.<init>"), DEFINITION, dataNodeWithLabel("InputStream")));
        assertThat(aug, not(hasEdge(actionNodeWithLabel("FileInputStream.<init>"), DEFINITION, dataNodeWithLabel("Reader"))));
    }

    @Test
    public void encodesTransitiveDefinitionEdgesThroughArithmeticOperator() throws Exception {
    	AUGConfiguration conf = new AUGConfiguration(){{}};
        APIUsageExample aug = AUGBuilderTestUtils.buildAUG("double m() {\n" +
        		"  java.util.List c = new ArrayList();" +
                "  double d = c.size() + 1f;\n" +
                "  return d;\n" +
                "}", conf);

        assertThat(aug, hasEdge(actionNodeWithLabel("ArrayList.<init>"), DEFINITION, dataNodeWithLabel("List")));
        if (conf.buildTransitiveDataEdges)
        	assertThat(aug, hasEdge(actionNodeWithLabel("Collection.size()"), DEFINITION, dataNodeWithLabel("double")));
        if (conf.buildTransitiveDataEdges)
        	assertThat(aug, not(hasEdge(actionNodeWithLabel("ArrayList.<init>"), DEFINITION, dataNodeWithLabel("double"))));
    }
}
