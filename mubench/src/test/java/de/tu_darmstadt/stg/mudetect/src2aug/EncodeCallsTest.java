package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.RECEIVER;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeCallsTest {
    @Test
    public void addsCall() throws Exception {
        APIUsageExample aug = buildAUG("void m(Object o) { o.toString(); }");

        assertThat(aug, hasEdge(dataNodeWithLabel("Object"), RECEIVER, actionNodeWithLabel("Object.toString()")));
    }

    /**
     * I'm not sure we want static calls to have a receiver, because this makes them indistinguishable from instance
     * calls.
     */
    @Test
    public void addsStaticCall() throws Exception {
        APIUsageExample aug = buildAUG("void m() { C.staticMethod(); }");

        assertThat(aug, hasEdge(dataNodeWithLabel("C"), RECEIVER, actionNodeWithLabel("C.staticMethod()")));
    }

    @Test
    public void addsConstructorInvocation() throws Exception {
        APIUsageExample aug = buildAUG("void m() { new Object(); }");

        assertThat(aug, hasNode(actionNodeWithLabel("Object.<init>")));
    }

    @Test
    public void addsSuperConstructorCall() throws Exception {
        APIUsageExample aug = buildAUG("C() { super(); }");

        // TODO this label seems unintuitive, should be something like Object.<sinit>()?
        assertThat(aug, hasNode(actionNodeWithLabel("Object()")));
    }

    @Test
    public void encodesOnlyDirectReceiverEdges() throws Exception {
        AUGConfiguration conf = new AUGConfiguration();
		APIUsageExample aug = buildAUG("void m(java.io.File f) {\n" +
                "  java.io.InputStream is = new java.io.FileInputStream(f);\n" +
                "  is.read();\n" +
                "}", conf );
		
		if (conf.buildTransitiveDataEdges) {
	        assertThat(aug, hasEdge(actionNodeWithLabel("FileInputStream.<init>"), RECEIVER, actionNodeWithLabel("InputStream.read()")));
	        assertThat(aug, hasEdge(dataNodeWithLabel("InputStream"), RECEIVER, actionNodeWithLabel("InputStream.read()")));
	        assertThat(aug, not(hasEdge(dataNodeWithLabel("File"), RECEIVER, actionNodeWithLabel("InputStream.read()"))));
		}
    }

    @Test
    public void encodesOnlyDirectParameterEdges() throws Exception {
        AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG("void m(java.io.File f) {\n" +
                "  java.io.InputStream is = new java.io.FileInputStream(f);\n" +
                "  java.io.Reader r = new InputStreamReader(is);\n" +
                "}", conf);

        assertThat(aug, hasEdge(dataNodeWithLabel("File"), PARAMETER, actionNodeWithLabel("FileInputStream.<init>")));
        assertThat(aug, hasEdge(dataNodeWithLabel("InputStream"), PARAMETER, actionNodeWithLabel("InputStreamReader.<init>")));
		if (conf.buildTransitiveDataEdges) {
	        assertThat(aug, hasEdge(actionNodeWithLabel("FileInputStream.<init>"), PARAMETER, actionNodeWithLabel("InputStreamReader.<init>")));
	        assertThat(aug, not(hasEdge(dataNodeWithLabel("File"), PARAMETER, actionNodeWithLabel("InputStreamReader.<init>"))));
		}
    }

    @Test
    public void encodesTransitiveParameterEdgesThroughArithmeticOperators() throws Exception {
        AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG("Object m(java.util.List l) {\n" +
                "  return l.get(l.size() - 1);\n" +
                "}", conf);

		if (conf.buildTransitiveDataEdges)
			assertThat(aug, hasEdge(actionNodeWithLabel("Collection.size()"), PARAMETER, actionNodeWithLabel("List.get()")));
        assertThat(aug, hasEdge(dataNodeWithLabel("int"), PARAMETER, actionNodeWithLabel("List.get()")));
    }

    @Test
    public void encodesTransitiveParameterEdgesThroughBooleanOperators() throws Exception {
        AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG("boolean m(java.util.List l) {\n" +
                "  return !l.isEmpty();\n" +
                "}", conf);

		if (conf.buildTransitiveDataEdges)
			assertThat(aug, hasEdge(actionNodeWithLabel("Collection.isEmpty()"), PARAMETER, actionNodeWithLabel("return")));
        assertThat(aug, hasEdge(dataNodeWithLabel("boolean"), PARAMETER, actionNodeWithLabel("return")));
    }
}
