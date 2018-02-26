package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledEdgeMatcher;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.matcher.SubtypeDataNodeMatcher;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.ranking.NoRankingStrategy;
import de.tu_darmstadt.stg.mudetect.typehierarchy.TypeHierarchy;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.*;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static edu.iastate.cs.mudetect.mining.TestPatternBuilder.somePattern;
import static de.tu_darmstadt.stg.mudetect.utils.SetUtils.asSet;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUGForMethod;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AlternativePatternsIntegrationTest {

    @Test
    public void matchesSubtypes() {
        APIUsagePattern pattern = buildPattern("void p(Object o) { o.hashCode(); }", 2);
        APIUsageExample target = buildAUGForMethod("void t(Integer i) { i.hashCode(); }");
        TypeHierarchy typeHierarchy = new TypeHierarchy() {{ addSupertype("Integer", "Object"); }};
        MuDetect detector = new MuDetect(() -> asSet(pattern),
                new AlternativeMappingsOverlapsFinder(
                        new AlternativeMappingsOverlapsFinder.Config() {{
                            BaseAUGLabelProvider labelProvider = new BaseAUGLabelProvider();
                            nodeMatcher = new SubtypeDataNodeMatcher(typeHierarchy).or(new EquallyLabelledNodeMatcher(labelProvider));
                            edgeMatcher = new EquallyLabelledEdgeMatcher(labelProvider);
                        }}),
                new MissingElementViolationPredicate(),
                new NoRankingStrategy()::rankViolations);

        List<Violation> violations = detector.findViolations(asSet(target));

        assertThat(violations, is(empty()));
    }

    @Test
    public void multipleCallViolations() {
        APIUsagePattern pattern = somePattern(buildAUG().withActionNode("JPanel.<init>").withDataNode("JPanel")
                .withActionNode("add1", "JPanel.add()").withActionNode("add2", "JPanel.add()")
                .withEdge("JPanel.<init>", DEFINITION, "JPanel")
                .withEdge("JPanel", RECEIVER, "add1")
                .withEdge("JPanel", RECEIVER, "add2")
                .withEdge("add2", ORDER, "add1").build());
        APIUsageExample target = buildAUGForMethod("import javax.swing.JPanel;\n" +
                "class C {\n" +
                "  void m() {\n" +
                "    JPanel controlPanel = new JPanel();\n"+
                "    controlPanel.add(null);\n"+
                "    controlPanel.add(null);\n"+
                "    controlPanel.add(null);\n"+
                "    controlPanel.add(null);\n" +
                "  }\n" +
                "}");
        MuDetect detector = new MuDetect(() -> asSet(pattern),
                new AlternativeMappingsOverlapsFinder(
                        new AlternativeMappingsOverlapsFinder.Config() {{
                            BaseAUGLabelProvider labelProvider = new BaseAUGLabelProvider();
                            nodeMatcher = new EquallyLabelledNodeMatcher(labelProvider);
                            edgeMatcher = new EquallyLabelledEdgeMatcher(labelProvider);
                        }}),
                new MissingElementViolationPredicate(),
                new NoRankingStrategy()::rankViolations);

        List<Violation> violations = detector.findViolations(asSet(target));

        assertThat(violations, is(empty()));
    }

    @Test
    public void mappingToWrongDataNode() {
        APIUsagePattern pattern = somePattern(buildAUG()
                .withDataNode("IO1", "IndexOutput")
                .withActionNode("gFP()", "IndexOutput.getFilePointer()")
                .withEdge("IO1", RECEIVER, "gFP()")
                .withDataNode("long")
                .withEdge("gFP()", DEFINITION, "long")
                .withDataNode("IO2", "IndexOutput")
                .withActionNode("wL()", "IndexOutput.writeLong()")
                .withEdge("IO2", RECEIVER, "wL()")
                .withEdge("long", PARAMETER, "wL()"));
        APIUsageExample target = buildAUGForMethod("class C {\n" +
                "  IndexOutput tvd, tvf, tvx;" +
                "  void addRawDocuments(TermVectorsReader reader, int[] tvdLengths, int[] tvfLengths, int numDocs) throws IOException {\n" +
                "    long tvdPosition = tvd.getFilePointer();\n" +
                "    long tvfPosition = tvf.getFilePointer();\n" +
                "    long tvdStart = tvdPosition;\n" +
                "    long tvfStart = tvfPosition;\n" +
                "    for(int i=0;i<numDocs;i++) {\n" +
                "      tvx.writeLong(tvdPosition);\n" +
                "      tvdPosition += tvdLengths[i];\n" +
                "      tvx.writeLong(tvfPosition);\n" +
                "      tvfPosition += tvfLengths[i];\n" +
                "    }\n" +
                "    tvd.copyBytes(reader.getTvdStream(), tvdPosition-tvdStart);\n" +
                "    tvf.copyBytes(reader.getTvfStream(), tvfPosition-tvfStart);\n" +
                "  }\n" +
                "}");

        MuDetect detector = new MuDetect(() -> asSet(pattern),
                new AlternativeMappingsOverlapsFinder(
                        new AlternativeMappingsOverlapsFinder.Config() {{
                            BaseAUGLabelProvider labelProvider = new BaseAUGLabelProvider();
                            nodeMatcher = new EquallyLabelledNodeMatcher(labelProvider);
                            edgeMatcher = new EquallyLabelledEdgeMatcher(labelProvider);
                        }}),
                new MissingElementViolationPredicate(),
                new NoRankingStrategy()::rankViolations);

        List<Violation> violations = detector.findViolations(asSet(target));

        if (!violations.isEmpty()) {
            System.out.println(violations.get(0).getOverlap().getMissingEdges());
        }
        assertThat(violations, is(empty()));
    }

    private APIUsagePattern buildPattern(String method, int support) {
        return somePattern(buildAUGForMethod(method), support);
    }
}
