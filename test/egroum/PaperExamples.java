package egroum;

import graphics.DotGraph;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static egroum.EGroumTestUtils.buildGroumForMethod;

public class PaperExamples {
    @Test
    public void paperExample() throws Exception {
        printGroum("AUG",
                "void m() {" +
                "String path = \"/some/path.ext\";" +
                "if (path != null) {" +
                "  try {" +
                "    java.io.FileInputStream fis = new java.io.FileInputStream(path);" +
                "    return fis.read();" +
                "  } catch(java.io.FileNotFoundException e) {" +
                "    handle(e);" +
                "  }" +
                "}" +
                "}");
    }

    @Test
    public void dataNodesMotivation() throws Exception {
        printGroum("dataNodesMotivation-newO",
                "void m() {" +
                "  O o = new O();" +
                "  o.m();" +
                "  o.n();" +
                "}");

        printGroum("dataNodesMotivation-getO",
                "void m() {" +
                "  O o = getO();" +
                "  o.m();" +
                "  o.n();" +
                "}");
    }

    @Test
    public void controlVsDataFlow() throws Exception {
        printGroum("crtl-vs-data",
                "void m() {" +
                "  O o = new O();" +
                "  o.n();" +
                "}");
    }

    private void printGroum(String exampleName, String code) {
        EGroumGraph aug = buildGroumForMethod(code);
        List<EGroumNode> scaffoldNodes = aug.getNodes().stream()
                .filter(node -> node.getLabel().equals("C")).collect(Collectors.toList());
        for (EGroumNode scaffoldNode : scaffoldNodes) {
            aug.delete(scaffoldNode);
        }
        outputExample(exampleName, aug);
    }

    private void outputExample(String exampleName, EGroumGraph aug) {
        DotGraph dotGraph = new DotGraph(aug);
        System.out.println(dotGraph.getGraph());
        dotGraph.toPNG(new File("paper-examples"), exampleName);
    }
}
