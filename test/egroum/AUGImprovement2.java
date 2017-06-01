package egroum;

import graphics.DotGraph;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTNode;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import egroum.EGroumDataEdge.Type;

public class AUGImprovement2 {
    @Rule
    public TestName name = new TestName();

    @Test
    public void instanceOf() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	ArrayList<EGroumGraph> gs = EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_instanceof.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    	int c = 0;
    	for (EGroumNode node : gs.get(0).nodes)
    		if (node.getLabel().endsWith(".<instanceof>"))
    			c++;
    	Assert.assertThat(c, Is.is(1));
    }

    @Test
    public void nullcheck() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	ArrayList<EGroumGraph> gs = EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_null_check.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    	int c = 0;
    	for (EGroumNode node : gs.get(0).nodes)
    		if (node.getLabel().equals("<nullcheck>"))
    			c++;
    	Assert.assertThat(c, Is.is(1));
    }

    @Test
    public void filterAPI() throws Exception {
    	ArrayList<EGroumGraph> gs = EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_filter_API.java", null, "aug-improvement", new AUGConfiguration(){{}});
    	Assert.assertThat(gs.get(0).getNodes().size(), IsNot.not(0));
    	Assert.assertThat(gs.get(0).getEdges().size(), IsNot.not(0));
    	
    	gs = EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_filter_API.java", null, "aug-improvement", new AUGConfiguration(){{apiClasses = new String[]{};}});
    	Assert.assertThat(gs.get(0).getNodes().size(), Is.is(0));
    	Assert.assertThat(gs.get(0).getEdges().size(), Is.is(0));
    	
    	gs = EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_filter_API.java", null, "aug-improvement", new AUGConfiguration(){{apiClasses = new String[]{"java.util"};}});
    	Assert.assertThat(gs.get(0).getNodes().size(), Is.is(0));
    	Assert.assertThat(gs.get(0).getEdges().size(), Is.is(0));
    	
    	gs = EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_filter_API.java", null, "aug-improvement", new AUGConfiguration(){{apiClasses = new String[]{"java.util.Iterator"};}});
    	Assert.assertThat(gs.get(0).getNodes().size(), IsNot.not(0));
    	Assert.assertThat(gs.get(0).getEdges().size(), IsNot.not(0));
    	
    	gs = EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_filter_API.java", null, "aug-improvement", new AUGConfiguration(){{apiClasses = new String[]{"java.util.Collection"};}});
    	Assert.assertThat(gs.get(0).getNodes().size(), IsNot.not(0));
    	Assert.assertThat(gs.get(0).getEdges().size(), IsNot.not(0));
    	
    	gs = EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_filter_API.java", null, "aug-improvement", new AUGConfiguration(){{apiClasses = new String[]{"java.util.Collection", "java.util.Iterator"};}});
    	Assert.assertThat(gs.get(0).getNodes().size(), IsNot.not(0));
    	Assert.assertThat(gs.get(0).getEdges().size(), IsNot.not(0));
    }

    @Test
    public void constant() throws Exception {
    	ArrayList<EGroumGraph> gs = EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_constant.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2;}});
    	Assert.assertThat(gs.get(0).getNodes().size(), Is.is(16));
    	Assert.assertThat(gs.get(0).getEdges().size(), Is.is(38));
    	int c = 0;
    	for (EGroumNode node : gs.get(0).getNodes()) {
    		if (node.getAstNodeType() == ASTNode.NUMBER_LITERAL && node.getDataType().equals("int") && node.getDataName().equals("0"))
    			c++;
    		else if (node.getAstNodeType() == ASTNode.BOOLEAN_LITERAL && node.getDataType().equals("boolean") && node.getDataName().equals("true"))
    			c++;
    		else if (node.getAstNodeType() == ASTNode.CHARACTER_LITERAL && node.getDataType().equals("char") && node.getDataName().equals("c"))
    			c++;
    		else if (node.getAstNodeType() == ASTNode.STRING_LITERAL && node.getDataType().equals("String") && node.getDataName().equals("s"))
    			c++;
    	}
    	Assert.assertThat(c, Is.is(4));
    }

    @Test
    public void qualifiedName() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	ArrayList<EGroumGraph> gs = EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_qualified_name.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    	Assert.assertThat(gs.get(0).getNodes().size(), Is.is(9));
    	Assert.assertThat(gs.get(0).getEdges().size(), Is.is(12));
    }

    @Test
    public void cast() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	ArrayList<EGroumGraph> gs = EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_cast.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    	Assert.assertThat(gs.get(0).getNodes().size(), Is.is(6));
    	Assert.assertThat(gs.get(0).getEdges().size(), Is.is(8));
    	
    	boolean hasCastParameter = false;
    	for (EGroumEdge e : gs.get(1).getEdges()) {
    		if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).type == Type.PARAMETER 
    				&& e.source.getAstNodeType() == ASTNode.CAST_EXPRESSION && e.target.getAstNodeType() == ASTNode.METHOD_INVOCATION) {
    			hasCastParameter = true;
    			break;
    		}
    	}
    	Assert.assertThat(hasCastParameter, Is.is(true));
    }

    @Test
    public void dependentControl() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	/*ArrayList<EGroumGraph> gs = */EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_dependent_control.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    }

    @Test
    public void staticCall() throws Exception {
    	/*ArrayList<EGroumGraph> gs = */EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_alibaba2_old.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    }

    @Test
    public void operators() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_operators.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    }

    @Test
    public void qualifiedType() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_qualifiedType.java", null, "aug-improvement", new AUGConfiguration(){{groum = false;}});
    }

    @Test
    public void adjustControlEdges() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_adjustControlEdges.java", null, "aug-improvement", new AUGConfiguration(){{groum = false;}});
    }

    @Test
    public void deleteUnusedDataNodes() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_deleteUnusedDataNodes.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    }

    @Test
    public void switchstate() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_switch.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    }

    @Test
    public void itext() throws Exception {
    	EGroumBuilder gb = new EGroumBuilder(new AUGConfiguration());
    	gb.buildBatch("T:/repos/itext/5091/original-src", null);
    }

    @Ignore
    @Test
    public void eclipse_jdt_core() throws Exception {
    	EGroumBuilder gb = new EGroumBuilder(new AUGConfiguration());
    	gb.buildBatch("T:/repos/eclipse.jdt.core", null);
    }

    @Ignore
    @Test
    public void JetBrains_intellij_community() throws Exception {
    	EGroumBuilder gb = new EGroumBuilder(new AUGConfiguration());
    	gb.buildBatch("F:/github/repos-IntelliJ/JetBrains/intellij-community/java", null);
    }
    
    @Test
    public void closure() throws Exception {
    	EGroumBuilder gb = new EGroumBuilder(new AUGConfiguration(){{removeImplementationCode = 2;}});
    	gb.buildBatch("T:/repos/closure-compiler/src/", null);
    }
    
    @Ignore
    @Test
    public void nonDeterminism() throws Exception {
//    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_non_determinism.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 0; groum = false;}});
    	EGroumBuilder gb = new EGroumBuilder(new AUGConfiguration(){{removeImplementationCode = 2;}});
    	gb.buildBatch("T:/repos/closure-compiler/src/", null);
    }

    @Ignore
    @Test
    public void nonDeterminism1() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	ArrayList<EGroumGraph> gs = EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_non_determinism1.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 0; groum = false;}});
    	Assert.assertThat(gs.get(0).getNodes().size(), Is.is(9));
    	Assert.assertThat(gs.get(0).getEdges().size(), Is.is(16));
    }

    @Ignore
    @Test
    public void nonDeterminism2() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	ArrayList<EGroumGraph> gs = EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_non_determinism2.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 0; groum = false;}});
    	Assert.assertThat(gs.get(0).getNodes().size(), Is.is(10));
    	Assert.assertThat(gs.get(0).getEdges().size(), Is.is(18));
    }

    @Ignore
    @Test
    public void nonDeterminism3() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	/*ArrayList<EGroumGraph> gs = */EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_non_determinism3.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
//    	Assert.assertThat(gs.get(0).getNodes().size(), Is.is(9));
//    	Assert.assertThat(gs.get(0).getEdges().size(), Is.is(16));
    }

    @Test
    public void equals() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_equals.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 0; groum = false;}});
    }

    @Test
    public void receiver() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_receiver.java", null, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2; groum = false;}});
    }

    @Test
    public void foreach() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_foreach.java", null, "aug-improvement", new AUGConfiguration(){{}});
    }

    @Test
    public void removeConditionalOps() throws Exception {
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_conditional_op.java", null, "aug-improvement");
    }

    @Test
    public void collapse1() throws Exception {
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_collapse1.java", null, "aug-improvement");
    }

    @Test
    public void collapse2() throws Exception {
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_collapse2.java", null, "aug-improvement");
    }

    @Test
    public void keepData() throws Exception {
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_keep_data.java", null, "aug-improvement");
    }

    @Test
    public void order() throws Exception {
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_order.java", null, "aug-improvement");
    }

    @Test
    public void anonymousClass() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_anonymous_class.java", null, "aug-improvement");
    }

    @Test
    public void finallyEdge() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_finally.java", null, "aug-improvement");
    }

    @Test
    public void repeatEdges() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/synthetic/wait-loop/synthetic.wait-loop/review.php
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_control.java", null, "aug-improvement");
    }

    @Test
    public void syncEdges() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/synthetic/deadlock/synthetic.deadlock/no_findings.php
    	EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_sync.java", null, "aug-improvement");
    }

    @Test
    public void testTry() throws Exception {
        EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_try.java", null, "aug-improvement");
    }

    @Test
    public void tryWithResources() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/synthetic/fisexists/synthetic.fisexists/no_findings.php
        EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_try_resources.java", null, "aug-improvement");
    }

    @Test
    public void anonymousClassMethods() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/207/lucene.3/no_findings.php
        
    }

    @Test
    public void arrayType() throws Exception {
        EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_array_type.java", null, "aug-improvement");
    }

    @Test
    public void cme1() throws Exception {
        EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_cme1.java", new String[]{"test-resources/lib/guava-21.0.jar", "test-resources/lib/closure-compiler-v20170409.jar"}, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2;}});
    }

    @Test
    public void npe1() throws Exception {
        EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_npe1.java", new String[]{"test-resources/lib/guava-21.0.jar", "test-resources/lib/closure-compiler-v20170409.jar"}, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2;}});
    }

    @Test
    public void npe2() throws Exception {
        EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_npe2.java", new String[]{"test-resources/lib/guava-21.0.jar", "test-resources/lib/closure-compiler-v20170409.jar"}, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2;}});
    }

    @Test
    public void resolveType() throws Exception {
        EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_resolve_type.java", new String[]{"test-resources/lib/guava-21.0.jar", "test-resources/lib/closure-compiler-v20170409.jar"}, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2;}});
    }

    @Test
    public void parameterizedType() throws Exception {
        EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_parameterized_type.java", new String[]{"test-resources/lib/guava-21.0.jar", "test-resources/lib/closure-compiler-v20170409.jar"}, "aug-improvement", new AUGConfiguration(){{removeImplementationCode = 2;}});
    }

    @Test
    public void dataNode() throws Exception {
        EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_data_node.java", null, "aug-improvement");
    }

    @Test
    public void conditions() throws Exception {
        printGroum("void m(java.util.List l) { if (l.isEmpty()) l.get(); }");
        printGroum("void m(java.util.List l) { boolean b = l.isEmpty(); if (b) l.get(); }");
        printGroum("void m(java.util.List l) { boolean b = l.isEmpty(); if (b) l.get(); if (b) l.clear(); }");
        printGroum("void m(java.util.List l) { boolean b = l.isEmpty(); if (b) { l.get(); l.clear(); } }");
        printGroum("void m(java.util.List l) { if (l.isEmpty() && l.size() > 5) l.get(); }");
        printGroum("void m(java.util.List l) { if (!l.isEmpty()) l.get(); }");
    }

    @Test
    public void negation() throws Exception {
        printGroum("void m(java.util.List l) { if (!(l.isEmpty())) l.get(); }");
        printGroum("void m(java.util.List l) { if (!(l.isEmpty())) {} else { l.get(); } }");
    }

    private void printGroum(String code) {
        EGroumGraph aug = EGroumTestUtils.buildGroumForMethod(code);
        String s = new DotGraph(aug).getGraph();
        System.out.println(s);
    }
}
