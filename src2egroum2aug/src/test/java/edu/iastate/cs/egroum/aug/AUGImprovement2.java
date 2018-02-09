package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.DisplayAUGDotExporter;
import org.eclipse.jdt.core.dom.ASTNode;
import org.hamcrest.core.Is;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.ArrayList;

import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAndPrintAUGsForFile;
import static edu.iastate.cs.egroum.aug.EGroumDataEdge.Type.ORDER;
import static edu.iastate.cs.egroum.aug.TypeUsageExamplePredicate.usageExamplesOf;
import static edu.iastate.cs.egroum.aug.UsageExamplePredicate.allUsageExamples;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AUGImprovement2 {
    @Rule
    public TestName name = new TestName();

    @Test
    public void transitiveDataDependentEdges() throws Exception {
        ArrayList<EGroumGraph> gs = buildAndPrintAUGsForFile("input/Test_transitive_data_dependent_edges.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
        int c = 0;
        for (EGroumEdge edge : gs.get(0).getEdges())
            if (edge instanceof EGroumDataEdge
                    && ((EGroumDataEdge) edge).type == ORDER
                    && edge.source.getLabel().equals("AutoCloseable.close()")
                    && edge.target.getLabel().equals("ByteArrayOutputStream.toByteArray()"))
                c++;
        assertThat(c, Is.is(1));
    }


    @Test
    public void aug() {
    	buildAndPrintAUGsForFile("input/Example_09_14.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 0;}});
    }

    @Test
    public void alibaba_druid_1() {
    	buildAndPrintAUGsForFile("input/Test_alibaba_druid_1.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2;}});
    }

    @Test
    public void direct() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	ArrayList<EGroumGraph> gs = buildAndPrintAUGsForFile("input/Test_direct.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    	int c = 0;
    	for (EGroumNode node : gs.get(0).nodes)
    		if (node instanceof EGroumDataNode && node.getLabel().endsWith("String")) {
    			for (EGroumEdge e : node.outEdges)
    				if (e.isDirect())
    					c++;
    			break;
    		}
    	assertThat(c, Is.is(4));
    }

    @Test
    public void instanceOf() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	ArrayList<EGroumGraph> gs = buildAndPrintAUGsForFile("input/Test_instanceof.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    	int c = 0;
    	for (EGroumNode node : gs.get(0).nodes)
    		if (node.getLabel().endsWith(".<instanceof>"))
    			c++;
    	assertThat(c, Is.is(1));
    }

    @Test
    public void nullcheck() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	ArrayList<EGroumGraph> gs = buildAndPrintAUGsForFile("input/Test_null_check.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    	int c = 0;
    	for (EGroumNode node : gs.get(0).nodes)
    		if (node.getLabel().equals("<nullcheck>"))
    			c++;
    	assertThat(c, Is.is(1));
    }

    @Test
    public void filterAPI() {
    	ArrayList<EGroumGraph> gs = buildAndPrintAUGsForFile("input/Test_filter_API.java", null, "aug-improvement", new AUGConfiguration(){{}});
    	assertThat(gs, is(not(empty())));
    	
    	gs = buildAndPrintAUGsForFile("input/Test_filter_API.java", null, "aug-improvement", new AUGConfiguration(){{
            usageExamplePredicate = allUsageExamples();}});
    	assertThat(gs, is(not(empty())));
    	
    	gs = buildAndPrintAUGsForFile("input/Test_filter_API.java", null, "aug-improvement", new AUGConfiguration(){{
            usageExamplePredicate = usageExamplesOf("java.util");}});
    	assertThat(gs, is(empty()));
    	
    	gs = buildAndPrintAUGsForFile("input/Test_filter_API.java", null, "aug-improvement", new AUGConfiguration(){{
            usageExamplePredicate = usageExamplesOf("java.util.Iterator");}});
    	assertThat(gs, is(not(empty())));
    	
    	gs = buildAndPrintAUGsForFile("input/Test_filter_API.java", null, "aug-improvement", new AUGConfiguration(){{
            usageExamplePredicate = usageExamplesOf("java.util.Collection");}});
    	assertThat(gs, is(not(empty())));
    	
    	gs = buildAndPrintAUGsForFile("input/Test_filter_API.java", null, "aug-improvement", new AUGConfiguration(){{
            usageExamplePredicate = usageExamplesOf("java.util.Collection", "java.util.Iterator");}});
    	assertThat(gs, is(not(empty())));
    }

    @Test
    public void qualifiedName() {
    	AUGConfiguration conf = new AUGConfiguration(){{removeImplementationCode = 2; groum = false; buildTransitiveDataEdges = false;}};
    	ArrayList<EGroumGraph> gs = buildAndPrintAUGsForFile("input/Test_qualified_name.java", null, "aug-improvement", conf);
    	assertThat(gs.get(0).getNodes().size(), Is.is(9));
    	if (conf.buildTransitiveDataEdges)
    		assertThat(gs.get(0).getEdges().size(), Is.is(11));
    	else
    		assertThat(gs.get(0).getEdges().size(), Is.is(9));
    }

    @Test
    public void cast() {
    	AUGConfiguration conf = new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}};
    	ArrayList<EGroumGraph> gs = buildAndPrintAUGsForFile("input/Test_cast.java", null, "aug-improvement", conf);
    	assertThat(gs.get(0).getNodes().size(), Is.is(6));
    	if (conf.buildTransitiveDataEdges)
    		assertThat(gs.get(0).getEdges().size(), Is.is(8));
    	else
    		assertThat(gs.get(0).getEdges().size(), Is.is(6));
    	
    	if (conf.buildTransitiveDataEdges) {
	    	boolean hasCastParameter = false;
	    	for (EGroumEdge e : gs.get(1).getEdges()) {
	    		if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == EGroumDataEdge.Type.PARAMETER
	    				&& e.source.getAstNodeType() == ASTNode.CAST_EXPRESSION && e.target.getAstNodeType() == ASTNode.METHOD_INVOCATION) {
	    			hasCastParameter = true;
	    			break;
	    		}
	    	}
	    	assertThat(hasCastParameter, Is.is(true));
    	}
    }

    @Test
    public void dependentControl() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	/*ArrayList<EGroumGraph> gs = */
        buildAndPrintAUGsForFile("input/Test_dependent_control.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    }

    @Test
    public void staticCall() {
    	/*ArrayList<EGroumGraph> gs = */
        buildAndPrintAUGsForFile("input/Test_alibaba2_old.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    }

    @Test
    public void operators() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	buildAndPrintAUGsForFile("input/Test_operators.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    }

    @Test
    public void qualifiedType() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	buildAndPrintAUGsForFile("input/Test_qualifiedType.java", null, "aug-improvement", new AUGConfiguration(){{groum = false;}});
    }

    @Test
    public void adjustControlEdges() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	buildAndPrintAUGsForFile("input/Test_adjustControlEdges.java", null, "aug-improvement", new AUGConfiguration(){{groum = false;}});
    }

    @Test
    public void deleteUnusedDataNodes() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	buildAndPrintAUGsForFile("input/Test_deleteUnusedDataNodes.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    }

    @Test
    public void switchstate() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	buildAndPrintAUGsForFile("input/Test_switch.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    }

    @Ignore
    @Test
    public void itext() {
    	EGroumBuilder gb = new EGroumBuilder(new AUGConfiguration());
    	gb.buildBatch("T:/repos/itext/5091/original-src", null);
    }

    @Ignore
    @Test
    public void eclipse_jdt_core() {
    	EGroumBuilder gb = new EGroumBuilder(new AUGConfiguration());
    	gb.buildBatch("T:/repos/eclipse.jdt.core", null);
    }

    @Ignore
    @Test
    public void JetBrains_intellij_community() {
    	EGroumBuilder gb = new EGroumBuilder(new AUGConfiguration());
    	gb.buildBatch("F:/github/repos-IntelliJ/JetBrains/intellij-community/java", null);
    }

    @Ignore
    @Test
    public void closure() {
    	EGroumBuilder gb = new EGroumBuilder(new AUGConfiguration(){{removeImplementationCode = 2;}});
    	gb.buildBatch("T:/repos/closure-compiler/src/", null);
    }
    
    @Ignore
    @Test
    public void nonDeterminism() {
    	EGroumBuilder gb = new EGroumBuilder(new AUGConfiguration(){{removeImplementationCode = 2;}});
    	gb.buildBatch("T:/repos/closure-compiler/src/", null);
    }

    @Test
    public void nonDeterminism1() {
    	AUGConfiguration conf = new AUGConfiguration(){{removeImplementationCode = 0; keepQualifierEdges = true;}};
    	ArrayList<EGroumGraph> gs = buildAndPrintAUGsForFile("input/Test_non_determinism1.java", null, "aug-improvement", conf);
    	assertThat(gs.get(0).getNodes().size(), Is.is(8));
    	if (conf.buildTransitiveDataEdges)
    		assertThat(gs.get(0).getEdges().size(), Is.is(17));
    	else
    		assertThat(gs.get(0).getEdges().size(), Is.is(15));
    }

    @Test
    public void nonDeterminism2() {
    	AUGConfiguration conf = new AUGConfiguration(){{removeImplementationCode = 0; keepQualifierEdges = true;}};
    	ArrayList<EGroumGraph> gs = buildAndPrintAUGsForFile("input/Test_non_determinism2.java", null, "aug-improvement", conf);
    	assertThat(gs.get(0).getNodes().size(), Is.is(9));
    	if (conf.buildTransitiveDataEdges)
    		assertThat(gs.get(0).getEdges().size(), Is.is(17));
    	else
    		assertThat(gs.get(0).getEdges().size(), Is.is(19));
    }

    @Test
    public void nonDeterminism3() {
    	/*ArrayList<EGroumGraph> gs = */
        buildAndPrintAUGsForFile("input/Test_non_determinism3.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
//    	Assert.assertThat(gs.get(0).getNodes().size(), Is.is(9));
//    	Assert.assertThat(gs.get(0).getEdges().size(), Is.is(16));
    }

    @Test
    public void equals() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	buildAndPrintAUGsForFile("input/Test_equals.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 0; groum = false;}});
    }

    @Test
    public void receiver() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	buildAndPrintAUGsForFile("input/Test_receiver.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    }

    @Test
    public void foreach() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	buildAndPrintAUGsForFile("input/Test_foreach.java", null, "aug-improvement", new AUGConfiguration(){{}});
    }

    @Test
    public void removeConditionalOps() {
    	buildAndPrintAUGsForFile("input/Test_conditional_op.java", null, "aug-improvement");
    }

    @Test
    public void collapse1() {
    	buildAndPrintAUGsForFile("input/Test_collapse1.java", null, "aug-improvement");
    }

    @Test
    public void collapse2() {
    	buildAndPrintAUGsForFile("input/Test_collapse2.java", null, "aug-improvement");
    }

    @Test
    public void keepData() {
    	buildAndPrintAUGsForFile("input/Test_keep_data.java", null, "aug-improvement");
    }

    @Test
    public void order() {
    	buildAndPrintAUGsForFile("input/Test_order.java", null, "aug-improvement");
    }

    @Test
    public void anonymousClass() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	buildAndPrintAUGsForFile("input/Test_anonymous_class.java", null, "aug-improvement");
    }

    @Test
    public void finallyEdge() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	buildAndPrintAUGsForFile("input/Test_finally.java", null, "aug-improvement");
    }

    @Test
    public void repeatEdges() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/synthetic/wait-loop/synthetic.wait-loop/review.php
    	buildAndPrintAUGsForFile("input/Test_control.java", null, "aug-improvement");
    }

    @Test
    public void syncEdges() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/synthetic/deadlock/synthetic.deadlock/no_findings.php
    	buildAndPrintAUGsForFile("input/Test_sync.java", null, "aug-improvement");
    }

    @Test
    public void testTry() {
        buildAndPrintAUGsForFile("input/Test_try.java", null, "aug-improvement");
    }

    @Test
    public void tryWithResources() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/synthetic/fisexists/synthetic.fisexists/no_findings.php
        buildAndPrintAUGsForFile("input/Test_try_resources.java", null, "aug-improvement");
    }

    @Test
    public void anonymousClassMethods() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/207/lucene.3/no_findings.php
        
    }

    @Test
    public void arrayType() {
        buildAndPrintAUGsForFile("input/Test_array_type.java", null, "aug-improvement");
    }

    @Test
    public void cme1() {
        buildAndPrintAUGsForFile("input/Test_cme1.java", new String[]{"test-resources/lib/guava-21.0.jar", "test-resources/lib/closure-compiler-v20170409.jar"}, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2;}});
    }

    @Test
    public void npe1() {
        buildAndPrintAUGsForFile("input/Test_npe1.java", new String[]{"test-resources/lib/guava-21.0.jar", "test-resources/lib/closure-compiler-v20170409.jar"}, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2;}});
    }

    @Test
    public void npe2() {
        buildAndPrintAUGsForFile("input/Test_npe2.java", new String[]{"test-resources/lib/guava-21.0.jar", "test-resources/lib/closure-compiler-v20170409.jar"}, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2;}});
    }

    @Test
    public void resolveType() {
        buildAndPrintAUGsForFile("input/Test_resolve_type.java", new String[]{"test-resources/lib/guava-21.0.jar", "test-resources/lib/closure-compiler-v20170409.jar"}, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2;}});
    }

    @Test
    public void parameterizedType() {
        buildAndPrintAUGsForFile("input/Test_parameterized_type.java", new String[]{"test-resources/lib/guava-21.0.jar", "test-resources/lib/closure-compiler-v20170409.jar"}, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2;}});
    }

    @Test
    public void dataNode() {
        buildAndPrintAUGsForFile("input/Test_data_node.java", null, "aug-improvement");
    }

    @Test
    public void conditions() {
        printGroum("void m(java.util.List l) { if (l.isEmpty()) l.get(); }");
        printGroum("void m(java.util.List l) { boolean b = l.isEmpty(); if (b) l.get(); }");
        printGroum("void m(java.util.List l) { boolean b = l.isEmpty(); if (b) l.get(); if (b) l.clear(); }");
        printGroum("void m(java.util.List l) { boolean b = l.isEmpty(); if (b) { l.get(); l.clear(); } }");
        printGroum("void m(java.util.List l) { if (l.isEmpty() && l.size() > 5) l.get(); }");
        printGroum("void m(java.util.List l) { if (!l.isEmpty()) l.get(); }");
    }

    @Test
    public void negation() {
        printGroum("void m(java.util.List l) { if (!(l.isEmpty())) l.get(); }");
        printGroum("void m(java.util.List l) { if (!(l.isEmpty())) {} else { l.get(); } }");
    }

    private void printGroum(String code) {
        APIUsageExample aug = AUGBuilderTestUtils.buildAUGForMethod(code);
        System.out.println(new DisplayAUGDotExporter().toDotGraph(aug));
    }
}
