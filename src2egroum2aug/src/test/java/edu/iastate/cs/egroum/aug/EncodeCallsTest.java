package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasParameterEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasReceiverEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeCallsTest {
    @Test
    public void addsCall() {
        APIUsageExample aug = buildAUG("void m(Object o) { o.toString(); }");

        assertThat(aug, hasReceiverEdge(dataNodeWith(label("Object")), actionNodeWith(label("Object.toString()"))));
    }

    @Test
    public void addsSuperCall() {
        APIUsageExample aug = buildAUG("@override public boolean equals(Object other) { return super.equals(other); }");

        assertThat(aug, hasNode(actionNodeWith(label("Object.equals()"))));
    }

    /**
     * I'm not sure we want static calls to have a receiver, because this makes them indistinguishable from instance
     * calls.
     */
    @Test
    public void addsStaticCall() {
        APIUsageExample aug = buildAUG("void m() { C.staticMethod(); }");

        assertThat(aug, hasReceiverEdge(dataNodeWith(label("C")), actionNodeWith(label("C.staticMethod()"))));
    }

    @Test
    public void addsConstructorInvocation() {
        APIUsageExample aug = buildAUG("void m() { new Object(); }");

        assertThat(aug, hasNode(actionNodeWith(label("Object.<init>"))));
    }

    @Test
    public void addsSuperConstructorCall() {
        APIUsageExample aug = buildAUG("C() { super(); }");

        // TODO this label seems unintuitive, should be something like Object.<sinit>()?
        assertThat(aug, hasNode(actionNodeWith(label("Object()"))));
    }

    @Test
    public void encodesOnlyDirectReceiverEdges() {
        AUGConfiguration conf = new AUGConfiguration();
		APIUsageExample aug = buildAUG("void m(java.io.File f) {\n" +
                "  java.io.InputStream is = new java.io.FileInputStream(f);\n" +
                "  is.read();\n" +
                "}", conf );
		
		if (conf.buildTransitiveDataEdges) {
            assertThat(aug, hasReceiverEdge(actionNodeWith(label("FileInputStream.<init>")), actionNodeWith(label("InputStream.read()"))));
            assertThat(aug, hasReceiverEdge(dataNodeWith(label("InputStream")), actionNodeWith(label("InputStream.read()"))));
            assertThat(aug, not(hasReceiverEdge(dataNodeWith(label("File")), actionNodeWith(label("InputStream.read()")))));
		}
    }

    @Test
    public void encodesOnlyDirectParameterEdges() {
        AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG("void m(java.io.File f) {\n" +
                "  java.io.InputStream is = new java.io.FileInputStream(f);\n" +
                "  java.io.Reader r = new InputStreamReader(is);\n" +
                "}", conf);

        assertThat(aug, hasParameterEdge(dataNodeWith(label("File")), actionNodeWith(label("FileInputStream.<init>"))));
        assertThat(aug, hasParameterEdge(dataNodeWith(label("InputStream")), actionNodeWith(label("InputStreamReader.<init>"))));
		if (conf.buildTransitiveDataEdges) {
            assertThat(aug, hasParameterEdge(actionNodeWith(label("FileInputStream.<init>")), actionNodeWith(label("InputStreamReader.<init>"))));
            assertThat(aug, not(hasParameterEdge(dataNodeWith(label("File")), actionNodeWith(label("InputStreamReader.<init>")))));
		}
    }

    @Test
    public void encodesTransitiveParameterEdgesThroughArithmeticOperators() {
        AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG("Object m(java.util.List l) {\n" +
                "  return l.get(l.size() - 1);\n" +
                "}", conf);

		if (conf.buildTransitiveDataEdges)
            assertThat(aug, hasParameterEdge(actionNodeWith(label("Collection.size()")), actionNodeWith(label("List.get()"))));
        assertThat(aug, hasParameterEdge(dataNodeWith(label("int")), actionNodeWith(label("List.get()"))));
    }

    @Test
    public void encodesTransitiveParameterEdgesThroughBooleanOperators() {
        AUGConfiguration conf = new AUGConfiguration();
        APIUsageExample aug = buildAUG("boolean m(java.util.List l) {\n" +
                "  return !l.isEmpty();\n" +
                "}", conf);

		if (conf.buildTransitiveDataEdges)
            assertThat(aug, hasParameterEdge(actionNodeWith(label("Collection.isEmpty()")), actionNodeWith(label("<return>"))));
        assertThat(aug, hasParameterEdge(dataNodeWith(label("boolean")), actionNodeWith(label("<return>"))));
    }
}
