package egroum;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class AUGImprovement2 {
    @Rule
    public TestName name = new TestName();

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
    public void dataNode() throws Exception {
        EGroumTestUtils.buildAndPrintGroumsForFile("test-resources/input", "Test_data_node.java", null, "aug-improvement");
    }
}
