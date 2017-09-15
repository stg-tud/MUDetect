package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.matcher.SubtypeDataNodeMatcher;
import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.mining.Pattern;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.typehierarchy.TypeHierarchy;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mudetect.ranking.NoRankingStrategy;
import de.tu_darmstadt.stg.mudetect.ranking.PatternSupportWeightFunction;
import de.tu_darmstadt.stg.mudetect.ranking.WeightRankingStrategy;
import egroum.EGroumDataEdge;
import org.junit.Test;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.mining.TestPatternBuilder.somePattern;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumDataEdge.Type.DEFINITION;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static egroum.EGroumDataEdge.Type.RECEIVER;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static utils.SetUtils.asSet;

public class AlternativePatternsIntegrationTest {

    @Test
    public void matchesSubtypes() throws Exception {
        Pattern pattern = buildPattern("void p(Object o) { o.hashCode(); }", 2);
        AUG target = buildAUG("void t(Integer i) { i.hashCode(); }");
        TypeHierarchy typeHierarchy = new TypeHierarchy() {{ addSupertype("Integer", "Object"); }};
        MuDetect detector = new MuDetect(() -> asSet(pattern),
                new AlternativeMappingsOverlapsFinder(new SubtypeDataNodeMatcher(typeHierarchy).or(new EquallyLabelledNodeMatcher())),
                new MissingElementViolationPredicate(),
                new NoRankingStrategy());

        List<Violation> violations = detector.findViolations(asSet(target));

        assertThat(violations, is(empty()));
    }

    @Test
    public void multipleCallViolations() throws Exception {
        Pattern pattern = somePattern(TestAUGBuilder.buildAUG().withActionNode("JPanel.<init>").withDataNode("JPanel")
                .withActionNode("add1", "JPanel.add()").withActionNode("add2", "JPanel.add()")
                .withDataEdge("JPanel.<init>", DEFINITION, "JPanel")
                .withDataEdge("JPanel", RECEIVER, "add1")
                .withDataEdge("JPanel", RECEIVER, "add2")
                .withDataEdge("add2", EGroumDataEdge.Type.ORDER, "add1").build());
        AUG target = buildAUG("import javax.swing.JPanel;\n" +
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
                new AlternativeMappingsOverlapsFinder(new EquallyLabelledNodeMatcher()),
                new MissingElementViolationPredicate(),
                new NoRankingStrategy());

        List<Violation> violations = detector.findViolations(asSet(target));

        assertThat(violations, is(empty()));
    }

    @Test
    public void mappingToWrongDataNode() throws Exception {
        Pattern pattern = somePattern(TestAUGBuilder.buildAUG()
                .withDataNode("IO1", "IndexOutput")
                .withActionNode("gFP()", "IndexOutput.getFilePointer()")
                .withDataEdge("IO1", RECEIVER, "gFP()")
                .withDataNode("long")
                .withDataEdge("gFP()", DEFINITION, "long")
                .withDataNode("IO2", "IndexOutput")
                .withActionNode("wL()", "IndexOutput.writeLong()")
                .withDataEdge("IO2", RECEIVER, "wL()")
                .withDataEdge("long", PARAMETER, "wL()"));
        AUG target = buildAUG("class C {\n" +
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
                "    assert tvd.getFilePointer() == tvdPosition;\n" +
                "    assert tvf.getFilePointer() == tvfPosition;" +
                "  }\n" +
                "}");

        MuDetect detector = new MuDetect(() -> asSet(pattern),
                new AlternativeMappingsOverlapsFinder(new EquallyLabelledNodeMatcher()),
                new MissingElementViolationPredicate(),
                new NoRankingStrategy());

        List<Violation> violations = detector.findViolations(asSet(target));

        if (!violations.isEmpty()) {
            System.out.println(violations.get(0).getOverlap().getMissingEdges());
        }
        assertThat(violations, is(empty()));
    }

    private Pattern buildPattern(String method, int support) {
        return somePattern(buildAUG(method), support);
    }
}
