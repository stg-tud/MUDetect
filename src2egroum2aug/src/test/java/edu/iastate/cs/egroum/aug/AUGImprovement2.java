package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.AUGTestUtils;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.CastNode;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.TypeCheckNode;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.DisplayAUGDotExporter;
import org.eclipse.jdt.core.dom.ASTNode;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.Collection;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.type;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUGForMethod;
import static edu.iastate.cs.egroum.aug.TypeUsageExamplePredicate.usageExamplesOf;
import static edu.iastate.cs.egroum.aug.UsageExamplePredicate.allUsageExamples;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AUGImprovement2 {
    @Rule
    public TestName name = new TestName();

    @Test
    public void transitiveDataDependentEdges() throws Exception {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_transitive_data_dependent_edges.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
            groum = false;
        }});
        APIUsageExample aug = augs.iterator().next();

        assertThat(aug, hasOrderEdge(methodCall("AutoCloseable", "close()"), methodCall("ByteArrayOutputStream", "toByteArray()")));
    }

    @Test
    public void aug() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Example_09_14.java", new AUGConfiguration() {{
            removeImplementationCode = 0;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void alibaba_druid_1() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_alibaba_druid_1.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void direct() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_direct.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
            groum = false;
        }});
        APIUsageExample aug = augs.iterator().next();

        assertThat(aug.getEdgeSize(), is(7));
        // TODO translate transitive edges to AUGs
        //assertThat(aug.edgeSet().stream().filter(Edge::isDirect).count(), is(4));
    }

    @Test
    public void instanceOf() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_instanceof.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
            groum = false;
        }});

        APIUsageExample aug = augs.iterator().next();
        assertThat(aug.vertexSet().stream().filter(node -> node instanceof TypeCheckNode).count(), is(1L));
    }

    @Test
    public void nullcheck() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_null_check.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
            groum = false;
        }});

        APIUsageExample aug = augs.iterator().next();
        assertThat(aug, hasNode(actionNodeWith(label("<nullcheck>"))));
    }

    @Test
    public void filterAPI() {
        assertThat(buildAUGsFromFile("input/Test_filter_API.java", new AUGConfiguration() {{
        }}), is(not(empty())));

        assertThat(buildAUGsFromFile("input/Test_filter_API.java", new AUGConfiguration() {{
            usageExamplePredicate = allUsageExamples();
        }}), is(not(empty())));

        assertThat(buildAUGsFromFile("input/Test_filter_API.java", new AUGConfiguration() {{
            usageExamplePredicate = usageExamplesOf("java.util");
        }}), is(empty()));

        assertThat(buildAUGsFromFile("input/Test_filter_API.java", new AUGConfiguration() {{
            usageExamplePredicate = usageExamplesOf("java.util.Iterator");
        }}), is(not(empty())));

        assertThat(buildAUGsFromFile("input/Test_filter_API.java", new AUGConfiguration() {{
            usageExamplePredicate = usageExamplesOf("java.util.Collection");
        }}), is(not(empty())));

        assertThat(buildAUGsFromFile("input/Test_filter_API.java", new AUGConfiguration() {{
            usageExamplePredicate = usageExamplesOf("java.util.Collection", "java.util.Iterator");
        }}), is(not(empty())));
    }

    @Test
    public void constant() throws Exception {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_constant.java", new AUGConfiguration(){{removeImplementationCode = 2;}});
        assertThat(augs.size(), is(1));
        APIUsageExample aug = augs.iterator().next();

        assertThat(aug.getNodeSize(), Is.is(19));
        assertThat(aug.getEdgeSize(), Is.is(60));

        // TODO capturing of names and values is inconsistent
        assertThat(aug, hasConstantNode("int", "Test_constant.n", "0"));
        assertThat(aug, hasConstantNode("char", "c", null));
        assertThat(aug, hasConstantNode("String", "s", null));
        assertThat(aug, hasConstantNode("boolean", "b", null));
        assertThat(aug, hasConstantNode("int", "Integer.MAX_VALUE", String.valueOf(Integer.MAX_VALUE)));
    }

    @Test
    public void qualifiedName() {
        AUGConfiguration conf = new AUGConfiguration() {{
            removeImplementationCode = 2;
            groum = false;
            encodeQualifiers = true;
        }};
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_qualified_name.java", conf);

        APIUsageExample aug = augs.iterator().next();
        assertThat(aug.getNodeSize(), Is.is(9));
        if (conf.buildTransitiveDataEdges)
            assertThat(aug.getEdgeSize(), Is.is(11));
        else
            assertThat(aug.getEdgeSize(), Is.is(9));
    }

    @Test
    public void catch_1() {
        buildAUGsFromFile("input/Test_catch.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
            groum = false;
        }});
    }

    @Test
    public void catch_2() {
        buildAUGsFromFile("input/SocialNetworkDatabaseBoards.java", new AUGConfiguration(){{
            removeImplementationCode = 2;
            groum = false;
        }});
    }

    @Test
    public void cast() {
        AUGConfiguration conf = new AUGConfiguration() {{
            removeImplementationCode = 2;
            groum = false;
        }};
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_cast.java", conf);

        APIUsageExample aug = augs.iterator().next();
        assertThat(aug.getNodeSize(), Is.is(6));
        if (conf.buildTransitiveDataEdges)
            assertThat(aug.getEdgeSize(), Is.is(8));
        else
            assertThat(aug.getEdgeSize(), Is.is(6));

        if (conf.buildTransitiveDataEdges) {
            assertThat(aug, hasParameterEdge(nodeWith(type(CastNode.class)), nodeWith(type(MethodCallNode.class))));
        }
    }

    @Test
    public void dependentControl() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
        /*ArrayList<EGroumGraph> gs = */
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_dependent_control.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
            groum = false;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void staticCall() {
        /*ArrayList<EGroumGraph> gs = */
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_alibaba2_old.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
            groum = false;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void operators() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_operators.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
            groum = false;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void qualifiedType() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_qualifiedType.java", new AUGConfiguration() {{
            groum = false;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void adjustControlEdges() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_adjustControlEdges.java", new AUGConfiguration() {{
            groum = false;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void deleteUnusedDataNodes() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_deleteUnusedDataNodes.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
            groum = false;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void switchstate() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_switch.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
            groum = false;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void nonDeterminism1() {
        AUGConfiguration conf = new AUGConfiguration() {{
            removeImplementationCode = 0;
            keepQualifierEdges = true;
        }};
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_non_determinism1.java", conf);

        APIUsageExample aug = augs.iterator().next();
        assertThat(aug.getNodeSize(), Is.is(8));
        if (conf.buildTransitiveDataEdges)
            assertThat(aug.getEdgeSize(), Is.is(17));
        else
            assertThat(aug.getEdgeSize(), Is.is(16));
    }

    @Test
    public void nonDeterminism2() {
        AUGConfiguration conf = new AUGConfiguration() {{
            removeImplementationCode = 0;
            keepQualifierEdges = true;
        }};
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_non_determinism2.java", conf);
        APIUsageExample aug = augs.iterator().next();
        assertThat(aug.getNodeSize(), Is.is(9));
        if (conf.buildTransitiveDataEdges)
            assertThat(aug.getEdgeSize(), Is.is(17));
        else
            assertThat(aug.getEdgeSize(), Is.is(19));
    }

    @Test
    public void nonDeterminism3() {
        /*ArrayList<EGroumGraph> gs = */
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_non_determinism3.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
            groum = false;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void equals() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_equals.java", new AUGConfiguration() {{
            removeImplementationCode = 0;
            groum = false;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void receiver() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_receiver.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
            groum = false;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void foreach() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_foreach.java", new AUGConfiguration() {{
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void removeConditionalOps() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_conditional_op.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void collapse1() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_collapse1.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void collapse2() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_collapse2.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void keepData() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_keep_data.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void order() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_order.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void anonymousClass() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_anonymous_class.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void finallyEdge() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/1251/lucene.1/review.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_finally.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void definition() throws Exception {
        buildAUGsFromFile("input/Test_definition.java", new AUGConfiguration());
    }

    @Test
    public void repeatEdges() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/synthetic/wait-loop/synthetic.wait-loop/review.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_control.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void syncEdges() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/synthetic/deadlock/synthetic.deadlock/no_findings.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_sync.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void testTry() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_try.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void tryWithResources() {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/synthetic/fisexists/synthetic.fisexists/no_findings.php
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_try_resources.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void arrayType() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_array_type.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void cme1() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_cme1.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void npe1() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_npe1.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void npe2() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_npe2.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void resolveType() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_resolve_type.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void parameterizedType() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_parameterized_type.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void dataNode() {
        Collection<APIUsageExample> augs = buildAUGsFromFile("input/Test_data_node.java", new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});

        //exportAUGsAsPNG(augs);
    }

    @Test
    public void conditions() {
        printDotGraph(buildAUGForMethod("void m(java.util.List l) { if (l.isEmpty()) l.get(); }"));
        printDotGraph(buildAUGForMethod("void m(java.util.List l) { boolean b = l.isEmpty(); if (b) l.get(); }"));
        printDotGraph(buildAUGForMethod("void m(java.util.List l) { boolean b = l.isEmpty(); if (b) l.get(); if (b) l.clear(); }"));
        printDotGraph(buildAUGForMethod("void m(java.util.List l) { boolean b = l.isEmpty(); if (b) { l.get(); l.clear(); } }"));
        printDotGraph(buildAUGForMethod("void m(java.util.List l) { if (l.isEmpty() && l.size() > 5) l.get(); }"));
        printDotGraph(buildAUGForMethod("void m(java.util.List l) { if (!l.isEmpty()) l.get(); }"));
    }

    @Test
    public void negation() {
        printDotGraph(buildAUGForMethod("void m(java.util.List l) { if (!(l.isEmpty())) l.get(); }"));
        printDotGraph(buildAUGForMethod("void m(java.util.List l) { if (!(l.isEmpty())) {} else { l.get(); } }"));
    }

    private void printDotGraph(APIUsageExample aug) {
        System.out.println(new DisplayAUGDotExporter().toDotGraph(aug));
    }

    private static Collection<APIUsageExample> buildAUGsFromFile(String inputPath, AUGConfiguration config) {
        return AUGBuilderTestUtils.buildAUGsFromFile(inputPath, config);
    }

    private void exportAUGsAsPNG(Collection<APIUsageExample> augs) {
        AUGTestUtils.exportAUGsAsPNG(augs, "./aug-improvement", name.getMethodName());
    }
}
